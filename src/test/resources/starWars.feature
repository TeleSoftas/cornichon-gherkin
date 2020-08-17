Feature: Star Wars API
  see https://swapi.co/

  Scenario: check out Luke Skywalker
    When I get https://swapi.dev/api/people/1/
    Then response code is 200
    And response body with whitelisting is
    """
    {
      "name": "Luke Skywalker",
      "height": "172",
      "mass": "77",
      "hair_color": "blond",
      "skin_color": "fair",
      "eye_color": "blue",
      "birth_year": "19BBY",
      "gender": "male",
      "homeworld": "http://swapi.dev/api/planets/1/",
      "films": [
          "http://swapi.dev/api/films/1/",
          "http://swapi.dev/api/films/2/",
          "http://swapi.dev/api/films/3/",
          "http://swapi.dev/api/films/6/"
      ],
      "species": [],
      "vehicles": [
          "http://swapi.dev/api/vehicles/14/",
          "http://swapi.dev/api/vehicles/30/"
      ],
      "starships": [
          "http://swapi.dev/api/starships/12/",
          "http://swapi.dev/api/starships/22/"
      ],
      "created": "2014-12-09T13:50:51.644000Z",
      "edited": "2014-12-20T21:17:56.891000Z",
      "url": "http://swapi.dev/api/people/1/"
    }
    """

    When I get https://swapi.dev/api/planets/1/
    Then response body with whitelisting is
    """
    {
      "name": "Tatooine",
      "rotation_period": "23",
      "orbital_period": "304",
      "diameter": "10465",
      "climate": "arid",
      "gravity": "1 standard",
      "terrain": "desert",
      "surface_water": "1",
      "population": "200000",
      "residents": [
          "http://swapi.dev/api/people/1/",
          "http://swapi.dev/api/people/2/",
          "http://swapi.dev/api/people/4/",
          "http://swapi.dev/api/people/6/",
          "http://swapi.dev/api/people/7/",
          "http://swapi.dev/api/people/8/",
          "http://swapi.dev/api/people/9/",
          "http://swapi.dev/api/people/11/",
          "http://swapi.dev/api/people/43/",
          "http://swapi.dev/api/people/62/"
      ],
      "films": [
          "http://swapi.dev/api/films/1/",
          "http://swapi.dev/api/films/3/",
          "http://swapi.dev/api/films/4/",
          "http://swapi.dev/api/films/5/",
          "http://swapi.dev/api/films/6/"
      ],
      "created": "2014-12-09T13:50:49.641000Z",
      "edited": "2014-12-20T20:58:18.411000Z",
      "url": "http://swapi.dev/api/planets/1/"
    }
    """

  Scenario: Load list of planets
    When I get https://swapi.dev/api/planets/
    And response body at path results with whitelisting is
      | name        | rotation_period | orbital_period | diameter | climate               |
      | "Tatooine"  | "23"            | "304"          | "10465"  | "arid"                |
      | "Alderaan"  | "24"            | "364"          | "12500"  | "temperate"           |
      | "Yavin IV"  | "24"            | "4818"         | "10200"  | "temperate, tropical" |
      | "Hoth"      | "23"            | "549"          | "7200"   | "frozen"              |
      | "Dagobah"   | "23"            | "341"          | "8900"   | "murky"               |
      | "Bespin"    | "12"            | "5110"         | "118000" | "temperate"           |
      | "Endor"     | "18"            | "402"          | "4900"   | "temperate"           |
      | "Naboo"     | "26"            | "312"          | "12120"  | "temperate"           |
      | "Coruscant" | "24"            | "368"          | "12240"  | "temperate"           |
      | "Kamino"    | "27"            | "463"          | "19720"  | "temperate"           |
