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
  // Route interception state. Each simulated failure is registered for ONE request
  // and every route is removed again when the scenario ends, so nothing leaks.
  simulated: async ({ page }, use) => {
    const registered = [];
    const hits = { PUT: 0, DELETE: 0 };
    const failNext = async (method, urlPattern, message) => {
      const handler = async (route) => {
        if (route.request().method() !== method) return route.fallback();
        hits[method] += 1;
        await route.fulfill({
          status: 404,
          contentType: 'application/json',
          body: JSON.stringify({
            error: 'Not Found',
            message,
            path: new URL(route.request().url()).pathname,
            timestamp: new Date().toISOString(),
          }),
        });
      };
      // times: 1 -> only the next matching request is faked; later ones reach the real API
      await page.route(urlPattern, handler, { times: 1 });
      registered.push({ urlPattern, handler });
    };
    await use({ failNext, hits });
    for (const { urlPattern, handler } of registered) {
      await page.unroute(urlPattern, handler);
    }
  },

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
