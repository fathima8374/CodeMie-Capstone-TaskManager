# To-Do List Application (Task Manager)

A Spring Boot REST API with a static HTML/CSS/JavaScript Task Manager UI.
Tasks have an optional due date, and the UI filters tasks (including **Overdue**) on the client side.
Tasks are kept in memory (a `HashMap`), so no database is needed and data is lost on restart.
The project is covered by automated Spring Boot API tests and Playwright/Gherkin UI tests.

## Features

- Create, read, update and delete tasks
- Optional due date (`yyyy-MM-dd`)
- Task Manager UI served by the application at `http://localhost:8080/`
- Client-side filters: **All**, **Active**, **Completed**, **Overdue**
- Validation and standardized JSON error responses
- Automated API tests (Spring Boot) and UI tests (Playwright + Gherkin)

## Tech Stack

- Java 17, Spring Boot 4, Spring Web, Bean Validation, Maven
- Static HTML / CSS / vanilla JavaScript (no framework)
- JUnit 5 + MockMvc for API tests
- Playwright + `playwright-bdd` (Gherkin) for UI tests, Node.js 18+

## Project Structure

```
src/
 ├── main/
 │    ├── java/com/sample/
 │    │     ├── controller/   REST endpoints (ToDoList_Controller)
 │    │     ├── entity/       Task model (ToDoList)
 │    │     ├── service/      In-memory business logic (ToDoList_Service)
 │    │     └── error/        ApiError, NotFoundException, GlobalExceptionHandler
 │    └── resources/static/   index.html, styles.css, app.js (the UI)
 └── test/
      ├── java/com/sample/controller/   API tests (ToDoList_ControllerApiTest)
      ├── resources/features/           Gherkin feature files
      └── e2e/steps/                    Playwright step definitions and fixtures
playwright.config.js, package.json      Playwright / Gherkin setup
```

## API

| Operation | Request | Success | Errors |
|---|---|---|---|
| Add task | `POST /todo` | `201 Created` + task | `400` |
| Get all | `GET /todo/readall` | `200` | |
| Get by id | `GET /todo/read/{id}` | `200` | `404` |
| Update | `PUT /todo/update/{id}` | `200` + task | `400`, `404` |
| Delete | `DELETE /todo/delete/{id}` | `204 No Content` (no body) | `404` |

Task body:

```json
{ "title": "Learn Java", "completed": false, "dueDate": "2030-05-17" }
```

- `title` is required and must not be blank.
- `dueDate` is optional, must be a real calendar date in `yyyy-MM-dd` (e.g. `2030-02-31` is rejected).
- Responses include the generated `tId`.

Every error returns this JSON (exactly these four fields) with status 400 or 404:

```json
{
  "error": "Not Found",
  "message": "Task with id 99 not found",
  "path": "/todo/read/99",
  "timestamp": "2026-10-02T08:57:49.192632800Z"
}
```

## Overdue rule

A task is **overdue** when it is **not completed** and its due date is **before the browser's local date**.
A task due **today** is **not** overdue. Tasks without a due date are never overdue.
Filtering is done in the browser; there is no server-side overdue API.

## How to Run

Prerequisites: JDK 17+ and Maven.

```
mvn spring-boot:run
```

Open `http://localhost:8080/` for the UI. The API is under `http://localhost:8080/todo`.

## Tests

### API tests (Spring Boot / MockMvc)

```
mvn clean test
```

Covers status codes (201, 200, 204, 400, 404), the `dueDate` format, validation, and the
four-field error structure. Each test starts and ends with an empty store (cleanup runs
in `@BeforeEach` / `@AfterEach`, so it also happens after a failed assertion).
Reports: `target/surefire-reports/`.

### UI tests (Playwright + Gherkin)

Prerequisites: Node.js 18+, plus a one-time setup:

```
npm install
npx playwright install chromium
```

Run:

```
npm run test:ui
```

This generates Playwright specs from the `.feature` files (`bddgen`) and runs them.
Playwright starts the application itself on port **8085** (the port must be free), so you
do not need to start it manually.

Feature files (`src/test/resources/features/`):

- `task-manager-basics.feature` – display, create with/without due date, complete/incomplete, delete, blank-title error
- `task-manager-filters.feature` – All / Active / Completed / Overdue, "due today is not overdue"
- `task-manager-errors.feature` – simulated `PUT` and `DELETE` 404 responses via Playwright route interception (test-side only; the backend is not changed)

Step definitions and fixtures: `src/test/e2e/steps/`.

**Reports** – `npm run report:ui` opens the HTML report (`playwright-report/`).
A JSON report is written to `test-results/results.json`. Failed scenarios keep a screenshot and a trace.

**Isolation and cleanup** – each scenario seeds its own tasks through the API (or creates them in the UI)
and the suite runs with one worker. An auto fixture requires an empty task list before each scenario, records the
ids it creates, and deletes only those tasks through the real `DELETE` API afterwards, even if the scenario failed.
Cleanup uses a separate HTTP client and runs after route interception has been removed, so simulated failures cannot affect it.
There is no reset endpoint in the application.

## Test Evidence

Last verified run (2026-10-02, branch `feature/EPMCDMETST-67023-task-manager-ui-dueDate-tests`):

| Suite | Command | Result |
|---|---|---|
| Spring Boot API tests | `mvn clean test` | 21 run, 0 failures, 0 errors – BUILD SUCCESS |
| Playwright + Gherkin UI tests | `npm run test:ui` | 14 passed, 0 failed |

UI scenarios: 6 basics, 6 filters/overdue, 2 simulated-404 error scenarios.

## Notes and Limitations

- Storage is in memory and not thread-safe; the UI tests therefore run with a single worker.
- UI tests run in Chromium only.
- Authentication and a real database are out of scope.

## Contributing

Pull requests are welcome. Feel free to open issues for improvements or bugs.
