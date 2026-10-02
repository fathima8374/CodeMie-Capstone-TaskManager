Feature: Task Manager friendly error handling
  Failures reported by the server are shown to the user and the real data is left untouched.
  The 404 responses are simulated in the browser (route interception); the backend is not changed.

  Scenario: A 404 while updating a task shows a friendly error
    Given the following tasks exist:
      | title        | dueDate | completed |
      | Flaky update |         | false     |
    And I open the Task Manager
    And the next update request fails with 404 and message "Task not found"
    When I mark the task "Flaky update" as completed
    Then the update request was intercepted
    And I see the error message "Task not found"
    And the task "Flaky update" still exists on the server
    And the task "Flaky update" is not completed

  Scenario: A 404 while deleting a task shows a friendly error
    Given the following tasks exist:
      | title        | dueDate | completed |
      | Flaky delete |         | false     |
    And I open the Task Manager
    And the next delete request fails with 404 and message "Task not found"
    When I delete the task "Flaky delete"
    Then the delete request was intercepted
    And I see the error message "Task not found"
    And the task "Flaky delete" still exists on the server
    And the task "Flaky delete" is listed
