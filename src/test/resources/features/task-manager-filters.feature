Feature: Task Manager filters and overdue
  Overdue means: not completed AND due date before today's local date.
  A task due today is NOT overdue.

  Background:
    Given the following tasks exist:
      | title          | dueDate   | completed |
      | Late open      | yesterday | false     |
      | Due today open | today     | false     |
      | Future open    | tomorrow  | false     |
      | No date open   |           | false     |
      | Late done      | yesterday | true      |
      | Future done    | tomorrow  | true      |
    And I open the Task Manager

  Scenario: All filter shows every task
    When I select the "All" filter
    Then the visible tasks are:
      | Late open | Due today open | Future open | No date open | Late done | Future done |

  Scenario: Active filter shows only incomplete tasks
    When I select the "Active" filter
    Then the visible tasks are:
      | Late open | Due today open | Future open | No date open |

  Scenario: Completed filter shows only completed tasks
    When I select the "Completed" filter
    Then the visible tasks are:
      | Late done | Future done |

  Scenario: Overdue filter shows only incomplete tasks due before today
    When I select the "Overdue" filter
    Then the visible tasks are:
      | Late open |

  Scenario: A task due today is not overdue
    When I select the "Overdue" filter
    Then the task "Due today open" is not listed
    When I select the "Active" filter
    Then the task "Due today open" is listed

  Scenario: Completing an overdue task removes it from the Overdue filter
    When I mark the task "Late open" as completed
    And I select the "Overdue" filter
    Then I see the empty task list message
