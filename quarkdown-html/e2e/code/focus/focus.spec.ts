import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("focuses lines 4-6 with reduced opacity on others", async (page) => {
    await expect(page.locator("code.focus-lines")).toBeVisible();

    // Lines 4, 5, 6 should have .focused class
    await expect(page.locator(".hljs-ln-line.focused")).toHaveCount(6); // 3 lines * 2 cells each

    // Non-focused lines should have reduced opacity
    const nonFocused = page.locator("code.focus-lines .hljs-ln-line:not(.focused)").first();
    const opacity = await nonFocused.evaluate((el) => parseFloat(getComputedStyle(el).opacity));
    expect(opacity).toBeLessThan(1);
});
