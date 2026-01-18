import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("splits code block across pages", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(2);

    // Each page has one code block
    const firstPageCode = pages.nth(0).locator("pre code.hljs");
    const secondPageCode = pages.nth(1).locator("pre code.hljs");
    await expect(firstPageCode).toBeAttached();
    await expect(secondPageCode).toBeAttached();
});

test("continues line numbers across split", async (page) => {
    const pages = page.locator(".pagedjs_page");

    // First page has lines 1, 2
    const firstPageLineNumbers = pages.nth(0).locator(".hljs-ln-n");
    await expect(firstPageLineNumbers).toHaveCount(2);
    await expect(firstPageLineNumbers.nth(0)).toHaveAttribute("data-line-number", "1");
    await expect(firstPageLineNumbers.nth(1)).toHaveAttribute("data-line-number", "2");

    // Second page has lines 3, 4
    const secondPageLineNumbers = pages.nth(1).locator(".hljs-ln-n");
    await expect(secondPageLineNumbers).toHaveCount(2);
    await expect(secondPageLineNumbers.nth(0)).toHaveAttribute("data-line-number", "3");
    await expect(secondPageLineNumbers.nth(1)).toHaveAttribute("data-line-number", "4");
});

test("preserves indentation across split", async (page) => {
    const pages = page.locator(".pagedjs_page");

    // Get all code lines from both pages
    const firstPageLines = pages.nth(0).locator("pre code.hljs .hljs-ln-code");
    const secondPageLines = pages.nth(1).locator("pre code.hljs .hljs-ln-code");

    // Line 1: 0 spaces, Line 2: 4 spaces
    const line1Text = await firstPageLines.nth(0).textContent();
    const line2Text = await firstPageLines.nth(1).textContent();
    expect(line1Text).toMatch(/^Line 1/);
    expect(line2Text).toMatch(/^ {4}Line 2/);

    // Line 3: 8 spaces, Line 4: 12 spaces
    const line3Text = await secondPageLines.nth(0).textContent();
    const line4Text = await secondPageLines.nth(1).textContent();
    expect(line3Text).toMatch(/^ {8}Line 3/);
    expect(line4Text).toMatch(/^ {12}Line 4/);
});
