// @ts-check
const { defineConfig, devices } = require('@playwright/test');
const { defineBddConfig } = require('playwright-bdd');

const PORT = 8085;
const BASE_URL = `http://localhost:${PORT}`;

// Gherkin features -> generated Playwright specs in .features-gen (git-ignored)
const testDir = defineBddConfig({
  features: 'src/test/resources/features/*.feature',
  steps: 'src/test/e2e/steps/*.js',
});

module.exports = defineConfig({
  testDir,
  // The app keeps tasks in one in-memory map, so scenarios must not run in parallel.
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 30_000,
  expect: { timeout: 5_000 },
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['json', { outputFile: 'test-results/results.json' }],
  ],
  use: {
    baseURL: BASE_URL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: `mvn -q spring-boot:run -Dspring-boot.run.arguments=--server.port=${PORT}`,
    url: `${BASE_URL}/todo/readall`,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    stdout: 'ignore',
    stderr: 'pipe',
  },
});
