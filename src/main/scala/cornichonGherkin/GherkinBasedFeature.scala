package cornichonGherkin

import java.io.InputStreamReader

import com.github.agourlay.cornichon.CornichonFeature
import com.github.agourlay.cornichon.core.{FeatureDef, Step, Scenario => ScenarioDef}
import com.github.agourlay.cornichon.steps.wrapped.AttachStep
import gherkin.ast.{Background, GherkinDocument, Tag, Feature => GFeature, Scenario => GScenario, Step => GStep}
import gherkin.{AstBuilder, Parser}

import scala.collection.JavaConverters._

trait GherkinBasedFeature extends CornichonFeature with GherkinStepHelper with ExtractorHelper with ColumnHelper {
  import GherkinBasedFeature._

  def featureFile: String = discoverFeature(this.getClass)
  def stepDefinitions: List[GherkinStep]

  lazy val feature = generateFeature(loadFeature(featureFile), stepDefinitions)

  lazy val Before = cornichonGherkin.Before
  lazy val After = cornichonGherkin.After
  lazy val Around = cornichonGherkin.Around
}

object GherkinBasedFeature {
  val FocusTag = "@focus"
  val IgnoreTag = "@ignore"
  val PendingTag = "@pending"

  def discoverFeature(clazz: Class[_]) = {
    val mat = """^(\w)(.*)Feature$""".r.pattern.matcher(clazz.getSimpleName)

    if (mat.matches())
      mat.group(1).toLowerCase + mat.group(2) + ".feature"
    else
      throw GherkinError(s"Can't infer the feature filer name based on the class name '${clazz.getSimpleName}'. Please rename the test with `Feature` suffix or override `featureFile` method.").toException
  }


  def loadFeature(fileName: String): GherkinDocument =
    getClass.getResourceAsStream("/" + fileName) match {
      case null ⇒ throw GherkinError("Can't find feature file: " + fileName).toException
      case stream ⇒ new Parser(new AstBuilder).parse(new InputStreamReader(stream))
    }

  def generateFeature(doc: GherkinDocument, stepDefinitions: List[GherkinStep]) = {
    Option(doc.getFeature).fold(FeatureDef("[Empty]", Nil))(f ⇒ defineFeature(f, GherkinStepColl(stepDefinitions, goodTags(f.getTags))))
  }

  def defineFeature(feature: GFeature, stepDefinitions: GherkinStepColl): FeatureDef = {
    val backgroundSteps = feature.getChildren.asScala.flatMap {
      case b: Background ⇒ b.getSteps.asScala
      case _ ⇒ Nil
    }

    val scenarios = feature.getChildren.asScala.collect {case s: GScenario ⇒ s}

    val focused = scenarios filter (s ⇒ stepDefinitions.isFocused(goodTags(s.getTags)))

    val cScenarios = scenarios.map { s ⇒
      val tags = goodTags(s.getTags)
      val ignored = (focused.nonEmpty && !stepDefinitions.isFocused(tags)) || stepDefinitions.isIgnored(tags)
      val pending = stepDefinitions.isPending(tags)

      val beforeSteps = stepDefinitions.before(tags)
      val afterSteps = stepDefinitions.after(tags)
      val bgSteps = backgroundSteps.map(defineStep(_, stepDefinitions, tags))
      val steps = s.getSteps.asScala.map(defineStep(_, stepDefinitions, tags))
      val allSteps = bgSteps ++ beforeSteps ++ steps ++ afterSteps
      val aroundSteps = stepDefinitions.around(tags, allSteps.toList)
      

      ScenarioDef(s.getName, aroundSteps, ignored = ignored, pending = pending)
    }

    FeatureDef(feature.getName, cScenarios.toList)
  }

  private def goodTags(tags: java.util.List[Tag]) = tags.asScala.map(_.getName).toSet

  def defineStep(step: GStep, stepDefinitions: GherkinStepColl, tags: Set[String]): Step = {
    val foundStep =
      stepDefinitions.stepDefinitions.foldLeft(None: Option[Step]) {
        case (None, s) ⇒ s.steps(step)
        case (s @ Some(_), _) ⇒ s
      }
    
    foundStep match {
      case Some(s) ⇒
        s.setTitle(step.getText)

      case None ⇒
        val message = "Step definition not found for: " + step.getText

        GherkinStep.errorStep(step.getText, GherkinError(message))
    }
  }
}

case class GherkinStepColl(allDefinitions: List[GherkinStep], featureTags: Set[String]) {
  import GherkinBasedFeature.{FocusTag, IgnoreTag, PendingTag}

  lazy val stepDefinitions = allDefinitions.collect {case s: StepDefinition ⇒ s}
  lazy val before = allDefinitions.collect {case s: Before ⇒ s}
  lazy val after = allDefinitions.collect {case s: After ⇒ s}
  lazy val around = allDefinitions.collect {case s: Around ⇒ s}

  def isFocused(tags: Set[String]) = (tags ++ featureTags) contains FocusTag
  def isIgnored(tags: Set[String]) = (tags ++ featureTags) contains IgnoreTag
  def isPending(tags: Set[String]) = (tags ++ featureTags) contains PendingTag

  def before(tags: Set[String]): List[Step] = {
    val allTags = tags ++ featureTags

    before.flatMap { b ⇒
      val inter = b.tags.intersect(allTags)

      if (inter.nonEmpty) b.steps(inter.head) else Nil
    }
  }

  def after(tags: Set[String]): List[Step] = {
    val allTags = tags ++ featureTags

    after.flatMap { a ⇒
      val inter = a.tags.intersect(allTags)

      if (inter.nonEmpty) a.steps(inter.head) else Nil
    }
  }

  def around(tags: Set[String], s: List[Step]): List[Step] = {
    val allTags = tags ++ featureTags

    around.foldLeft(s) {
      case (acc, a) ⇒
        val inter = a.tags.intersect(allTags)

        if (inter.nonEmpty) a.steps(inter.head, acc) else acc
    }
  }
}

class SimpleGherkinFeature(override val featureFile: String, val stepDefinitions: List[GherkinStep] = Nil) extends GherkinBasedFeature