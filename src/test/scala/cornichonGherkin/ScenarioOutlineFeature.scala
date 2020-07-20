package cornichonGherkin

class ScenarioOutlineFeature extends GherkinBasedFeature {
  lazy val stepDefinitions = Steps {
    step"I get ${strArg.ph}: ${getParams.opt}" { (url, params) ⇒
      When I get(url.toString).withParams(params.map(_.toList).getOrElse(Nil): _*)
    }

    step"response code is $intArg" { code ⇒
      Then assert status.is(code)
    }

    After("@showSession") {
      show_session
    }
  }

  lazy val getParams = tableArg(
    Column.parseTable(_,
      ColumnGroup(matchAll, stringColumn("Param"), stringColumn("Value"))(
        (_, name, value) ⇒ Right(name → value)))
      .map(_ groupBy (_._1) mapValues (_.head._2)))
}