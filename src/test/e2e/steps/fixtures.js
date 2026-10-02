const { expect } = require('@playwright/test');
const { test: base, createBdd } = require('playwright-bdd');

/** Deletes every task through the public API (test-side cleanup only). */
async function deleteAllTasks(request) {
  const res = await request.get('/todo/readall');
  expect(res.ok()).toBeTruthy();
  for (const task of await res.json()) {
    const del = await request.delete(`/todo/delete/${task.tId}`);
    expect([204, 404]).toContain(del.status());
  }
}

const test = base.extend({
  // Auto fixture: every scenario starts and ends with an empty task list,
  // and the teardown runs even when the scenario fails.
  cleanTasks: [
    async ({ request }, use) => {
      await deleteAllTasks(request);
      await use();
      await deleteAllTasks(request);
    },
    { auto: true },
  ],
});

const { Given, When, Then } = createBdd(test);

module.exports = { test, expect, Given, When, Then };
