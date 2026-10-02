Feature: Task Manager basics
  As a user I want to add, complete and delete tasks with an optional due date

  Scenario: Task Manager is displayed with no tasks
    When I open the Task Manager
    Then I see the "Task Manager" heading
    And I see the add task form
    And I see the filters "All", "Active", "Completed" and "Overdue"
    And I see the empty task list message

  Scenario: Create a task with a title and due date
    Given I open the Task Manager
    When I add a task titled "Write report" with due date "2031-03-15"
    Then the task "Write report" is listed
    And the task "Write report" shows due date "2031-03-15"
    And the task "Write report" is not completed

  Scenario: Create a task without a due date
    Given I open the Task Manager
    When I add a task titled "Buy milk" without a due date
    Then the task "Buy milk" is listed
    And the task "Buy milk" shows no due date

  Scenario: Mark a task complete and incomplete again
    Given the following tasks exist:
      | title      | dueDate | completed |
      | Water plants |         | false     |
    And I open the Task Manager
    When I mark the task "Water plants" as completed
    Then the task "Water plants" is completed
    When I mark the task "Water plants" as not completed
    Then the task "Water plants" is not completed

  Scenario: Delete a task
    Given the following tasks exist:
      | title     | dueDate | completed |
      | Old task  |         | false     |
      | Keep task |         | false     |
    And I open the Task Manager
    When I delete the task "Old task"
    Then the task "Old task" is not listed
    And the task "Keep task" is listed

  Scenario: A task without a title is rejected with a friendly message
    Given I open the Task Manager
    When I add a task titled "   " without a due date
    Then I see the error message "Title is required."
    And I see the empty task list message
