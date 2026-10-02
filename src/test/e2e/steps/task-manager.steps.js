const { expect, Given, When, Then } = require('./fixtures');

// ---- helpers ----------------------------------------------------------

function isoLocal(date) {
  const p = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${p(date.getMonth() + 1)}-${p(date.getDate())}`;
}

/** Resolves "today" / "yesterday" / "tomorrow" relative to the local date; passes ISO dates through. */
function resolveDate(value) {
  if (!value) return null;
  const offsets = { yesterday: -1, today: 0, tomorrow: 1 };
  if (value in offsets) {
    const d = new Date();
    d.setDate(d.getDate() + offsets[value]);
    return isoLocal(d);
  }
  return value;
}

const taskItems = (page) => page.getByTestId('task-item');
const taskItem = (page, title) => taskItems(page).filter({ hasText: title });

// ---- seeding via API (test data setup) --------------------------------

Given('the following tasks exist:', async ({ request }, dataTable) => {
  for (const row of dataTable.hashes()) {
    const res = await request.post('/todo', {
      data: {
        title: row.title,
        completed: row.completed === 'true',
        dueDate: resolveDate(row.dueDate),
      },
    });
    expect(res.status()).toBe(201);
  }
});

// ---- navigation / display ---------------------------------------------

Given('I open the Task Manager', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByTestId('task-list')).toBeAttached();
});

Then('I see the {string} heading', async ({ page }, text) => {
  await expect(page.getByRole('heading', { level: 1, name: text })).toBeVisible();
});

Then('I see the add task form', async ({ page }) => {
  await expect(page.getByTestId('task-title-input')).toBeVisible();
  await expect(page.getByTestId('task-duedate-input')).toBeVisible();
  await expect(page.getByTestId('add-task-button')).toBeVisible();
});

Then('I see the filters {string}, {string}, {string} and {string}', async ({ page }, a, b, c, d) => {
  const ids = { All: 'filter-all', Active: 'filter-active', Completed: 'filter-completed', Overdue: 'filter-overdue' };
  for (const name of [a, b, c, d]) {
    await expect(page.getByTestId(ids[name])).toHaveText(name);
  }
});

Then('I see the empty task list message', async ({ page }) => {
  await expect(page.getByTestId('empty-state')).toBeVisible();
  await expect(taskItems(page)).toHaveCount(0);
});

// ---- creating tasks ----------------------------------------------------

When('I add a task titled {string} with due date {string}', async ({ page }, title, dueDate) => {
  await page.getByTestId('task-title-input').fill(title);
  await page.getByTestId('task-duedate-input').fill(resolveDate(dueDate));
  await page.getByTestId('add-task-button').click();
});

When('I add a task titled {string} without a due date', async ({ page }, title) => {
  await page.getByTestId('task-title-input').fill(title);
  await page.getByTestId('add-task-button').click();
});

// ---- toggling / deleting ----------------------------------------------

When('I mark the task {string} as completed', async ({ page }, title) => {
  await taskItem(page, title).getByTestId('task-toggle').check();
});

When('I mark the task {string} as not completed', async ({ page }, title) => {
  await taskItem(page, title).getByTestId('task-toggle').uncheck();
});

When('I delete the task {string}', async ({ page }, title) => {
  await taskItem(page, title).getByTestId('task-delete').click();
});

// ---- filters -----------------------------------------------------------

When('I select the {string} filter', async ({ page }, name) => {
  const button = page.getByTestId(`filter-${name.toLowerCase()}`);
  await button.click();
  await expect(button).toHaveClass(/active/);
});

// ---- assertions on the task list --------------------------------------

Then('the task {string} is listed', async ({ page }, title) => {
  await expect(taskItem(page, title)).toHaveCount(1);
});

Then('the task {string} is not listed', async ({ page }, title) => {
  await expect(taskItem(page, title)).toHaveCount(0);
});

Then('the task {string} shows due date {string}', async ({ page }, title, dueDate) => {
  await expect(taskItem(page, title)).toContainText(resolveDate(dueDate));
});

Then('the task {string} shows no due date', async ({ page }, title) => {
  await expect(taskItem(page, title)).toHaveCount(1);
  await expect(taskItem(page, title).locator('.due-date')).toHaveCount(0);
});

Then('the task {string} is completed', async ({ page }, title) => {
  await expect(taskItem(page, title).getByTestId('task-toggle')).toBeChecked();
});

Then('the task {string} is not completed', async ({ page }, title) => {
  await expect(taskItem(page, title).getByTestId('task-toggle')).not.toBeChecked();
});

Then('the visible tasks are:', async ({ page }, dataTable) => {
  const expected = dataTable.raw().flat();
  await expect(taskItems(page)).toHaveCount(expected.length);
  for (const title of expected) {
    await expect(taskItem(page, title)).toHaveCount(1);
  }
});

// ---- errors ------------------------------------------------------------

Then('I see the error message {string}', async ({ page }, message) => {
  await expect(page.getByTestId('error-banner')).toBeVisible();
  await expect(page.getByTestId('error-banner')).toHaveText(message);
});
