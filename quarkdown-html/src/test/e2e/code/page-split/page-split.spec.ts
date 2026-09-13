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

    // First page has lines 1, 2, 3
    const firstPageLineNumbers = pages.nth(0).locator(".hljs-ln-n");
    await expect(firstPageLineNumbers).toHaveCount(3);
    await expect(firstPageLineNumbers.nth(0)).toHaveAttribute("data-line-number", "1");
    await expect(firstPageLineNumbers.nth(1)).toHaveAttribute("data-line-number", "2");
    await expect(firstPageLineNumbers.nth(2)).toHaveAttribute("data-line-number", "3");

    // Second page has line 4
    const secondPageLineNumbers = pages.nth(1).locator(".hljs-ln-n");
    await expect(secondPageLineNumbers).toHaveCount(1);
    await expect(secondPageLineNumbers.nth(0)).toHaveAttribute("data-line-number", "4");
});

test("preserves indentation across split", async (page) => {
    const pages = page.locator(".pagedjs_page");

    // Get all code lines from both pages
    const firstPageLines = pages.nth(0).locator("pre code.hljs .hljs-ln-code");
    const secondPageLines = pages.nth(1).locator("pre code.hljs .hljs-ln-code");

    // Lines 1-3: 0, 4, 8 spaces
    expect(await firstPageLines.nth(0).textContent()).toMatch(/^Line 1/);
    expect(await firstPageLines.nth(1).textContent()).toMatch(/^ {4}Line 2/);
    expect(await firstPageLines.nth(2).textContent()).toMatch(/^ {8}Line 3/);

    // Line 4: 12 spaces
    expect(await secondPageLines.nth(0).textContent()).toMatch(/^ {12}Line 4/);
});
