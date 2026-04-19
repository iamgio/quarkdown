import {test, expect} from "@playwright/test";
import {compile} from "../../__util/compile";
import {getServerUrl} from "../../__util/global-setup";
import * as path from "path";

const WINDOWS_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

const MAC_USER_AGENT =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

const testDir = __dirname;
const outName = "keybinding-platform-awareness";

test.beforeAll(() => {
    compile(path.join(testDir, "main.qd"), outName);
});

async function navigateAndWaitForReady(page: import("@playwright/test").Page) {
    const url = `${getServerUrl()}/${outName}/`;
    await page.goto(url);
    await expect(page.locator(".quarkdown")).toBeVisible();
    await page.waitForFunction(() => (window as any).isReady());
}

test("displays default key names on Windows/Linux", async ({browser}) => {
    const context = await browser.newContext({userAgent: WINDOWS_USER_AGENT});
    const page = await context.newPage();

    await navigateAndWaitForReady(page);

    const kbds = page.locator(".keybinding kbd");
    await expect(kbds).toHaveCount(5);
    // Cmd+Shift+K
    await expect(kbds.nth(0)).toHaveText("Ctrl");
    await expect(kbds.nth(1)).toHaveText("Shift");
    await expect(kbds.nth(2)).toHaveText("K");
    // Alt+F4
    await expect(kbds.nth(3)).toHaveText("Alt");
    await expect(kbds.nth(4)).toHaveText("F4");

    await context.close();
});

test("displays macOS key symbols on Mac", async ({browser}) => {
    const context = await browser.newContext({userAgent: MAC_USER_AGENT});
    const page = await context.newPage();

    await navigateAndWaitForReady(page);

    const kbds = page.locator(".keybinding kbd");
    await expect(kbds).toHaveCount(5);
    // Cmd+Shift+K
    await expect(kbds.nth(0)).toHaveText("\u2318");  // ⌘
    await expect(kbds.nth(1)).toHaveText("\u21E7");  // ⇧
    await expect(kbds.nth(2)).toHaveText("K");
    // Alt+F4
    await expect(kbds.nth(3)).toHaveText("\u2325");  // ⌥
    await expect(kbds.nth(4)).toHaveText("F4");

    await context.close();
});
