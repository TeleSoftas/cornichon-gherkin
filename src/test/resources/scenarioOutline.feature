Feature: Star Wars API
  see https://swapi.co/

  Scenario Outline: check out Luke Skywalker at <path>
    When I get http://swapi.co/api/<path>/1/
    Then response code is <result>
    Examples:
      | path   | result |
      | people | 200    |
      | blabla | 404    |
