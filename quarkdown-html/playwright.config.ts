import {defineConfig, devices} from "@playwright/test";

export default defineConfig({
    testDir: "./src/test/e2e",
    globalSetup: "./src/test/e2e/__util/global-setup.ts",
    globalTeardown: "./src/test/e2e/__util/global-teardown.ts",
    fullyParallel: true,
    forbidOnly: !!process.env.CI,
    retries: process.env.CI ? 2 : 0,
    reporter: "html",
    use: {
        trace: "on-first-retry",
    },
    projects: [
        {
            name: "chromium",
            use: {...devices["Desktop Chrome"]},
        },
    ],
});
