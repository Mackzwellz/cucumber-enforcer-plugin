Feature: Example feature name

  Background: Example setup
    Given English given step that is detected but not restricted
    And English and step that is reused

  Rule: Non-duplicate names

    Scenario: Scenario name 1
      When Hrvatski when step that is reused
      And English and step that is reused
      Then English when step that has the same method name in another file
      And English when step that has the same method name in another file but different text

    Scenario Outline: Scenario name 2
      When Hrvatski when step that is reused
      And English and step that is reused

      Examples:
        | param1 |
        | value1 |
        | value2 |

    Scenario Template: Scenario name 3
      When Hrvatski when step that is reused
      And English and step that is reused

      Examples:
        | param2 |
        | value3 |
        | value4 |

  Rule: duplicate names

    Scenario: Scenario name 4
      When Hrvatski when step that is reused
      And English and step that is reused

    Scenario Outline: Scenario name 4
      When Hrvatski when step that is reused
      And English and step that is reused

      Examples:
        | param1 |
        | value1 |
        | value2 |

    Scenario Template: Scenario name 4
      When Hrvatski when step that is reused
      And English and step that is reused

      Examples:
        | param2 |
        | value3 |
        | value4 |
