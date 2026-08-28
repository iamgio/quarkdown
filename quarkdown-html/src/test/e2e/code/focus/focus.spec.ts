import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("focuses lines 4-6 with reduced opacity on others", async (page) => {
    const codeBlock = page.locator("code.focus-lines").first();
    await expect(codeBlock).toBeVisible();

    // Lines 4, 5, 6 should have .focused class
    await expect(codeBlock.locator(".hljs-ln-line.focused")).toHaveCount(6); // 3 lines * 2 cells each

    // Non-focused lines should have reduced opacity
    const nonFocused = codeBlock.locator(".hljs-ln-line:not(.focused)").first();
    const opacity = await nonFocused.evaluate((el) => parseFloat(getComputedStyle(el).opacity));
    expect(opacity).toBeLessThan(1);
});

test("focuses open-ended ranges", async (page) => {
    const codeBlocks = page.locator("code.focus-lines");
    await expect(codeBlocks).toHaveCount(3);

    // focus:{8..} focuses from line 8 to the last line (11).
    const startOnly = codeBlocks.nth(1);
    await expect(startOnly.locator(".hljs-ln-line.focused")).toHaveCount(8); // 4 lines * 2 cells each
    await expect(startOnly.locator('.hljs-ln-code[data-line-number="8"]')).toHaveClass(/focused/);
    await expect(startOnly.locator('.hljs-ln-code[data-line-number="11"]')).toHaveClass(/focused/);
    await expect(startOnly.locator('.hljs-ln-code[data-line-number="7"]')).not.toHaveClass(/focused/);

    // focus:{..3} focuses from the first line up to line 3.
    const endOnly = codeBlocks.nth(2);
    await expect(endOnly.locator(".hljs-ln-line.focused")).toHaveCount(6); // 3 lines * 2 cells each
    await expect(endOnly.locator('.hljs-ln-code[data-line-number="1"]')).toHaveClass(/focused/);
    await expect(endOnly.locator('.hljs-ln-code[data-line-number="3"]')).toHaveClass(/focused/);
    await expect(endOnly.locator('.hljs-ln-code[data-line-number="4"]')).not.toHaveClass(/focused/);
});
