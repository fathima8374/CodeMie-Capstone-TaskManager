const { expect } = require('@playwright/test');
const { test: base, createBdd } = require('playwright-bdd');

const taskIds = async (request) => {
  const res = await request.get('/todo/readall');
  expect(res.ok(), 'GET /todo/readall must succeed').toBeTruthy();
  return (await res.json()).map((t) => t.tId);
};

const test = base.extend({
  /**
   * Per-scenario task tracking and cleanup (auto fixture, so it wraps EVERY scenario).
   *
   * Setup:    the task list must be empty - fail fast instead of silently using or
   *           deleting data that this scenario did not create.
   * Tracking: tasks seeded through the API are recorded by id via seed(); tasks created
   *           through the UI are found afterwards because they are the only ones present
   *           that are not in the baseline.
   * Teardown: always runs (also after a failed assertion). It deletes only this
   *           scenario's tasks through the real DELETE API, and uses the `request`
   *           fixture, which is a separate HTTP client that page.route() cannot intercept.
   */
  scenarioTasks: [
    async ({ request }, use) => {
      const baseline = new Set(await taskIds(request));
      expect([...baseline], 'task list must be empty before a scenario starts').toEqual([]);

      const seededIds = new Set();
      const seed = async (task) => {
        const res = await request.post('/todo', { data: task });
        expect(res.status(), 'seeding a task must return 201').toBe(201);
        const created = await res.json();
        seededIds.add(created.tId);
        return created;
      };

      try {
        await use({ seed, seededIds });
      } finally {
        const leftovers = (await taskIds(request)).filter((id) => !baseline.has(id));
        const toDelete = new Set([...seededIds, ...leftovers]);
        const failures = [];
        for (const id of toDelete) {
          const del = await request.delete(`/todo/delete/${id}`);
          // 404 = the scenario itself already deleted it
          if (![204, 404].includes(del.status())) failures.push(`${id} -> ${del.status()}`);
        }
        const remaining = (await taskIds(request)).filter((id) => toDelete.has(id));
        expect(remaining, `tasks left after cleanup (${failures.join(', ')})`).toEqual([]);
      }
    },
    { auto: true },
  ],

  // Route interception. Each simulated failure applies to ONE request, and every route
  // is removed again at teardown. It depends on scenarioTasks, so Playwright tears it
  // down (unroute) BEFORE the task cleanup runs.
  simulated: async ({ page, scenarioTasks }, use) => {
    void scenarioTasks;
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
    try {
      await use({ failNext, hits });
    } finally {
      for (const { urlPattern, handler } of registered) {
        await page.unroute(urlPattern, handler);
      }
    }
  },
});

const { Given, When, Then } = createBdd(test);

module.exports = { test, expect, Given, When, Then };
