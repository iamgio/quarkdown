import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const LINE_COUNT = 20;

test("keeps table and code lines intact around page breaks", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(3);

    // The table is intact, with its header row and content.
    const table = pages.nth(0).locator("table:not(.hljs-ln)");
    await expect(table.locator("thead th")).toHaveText("Name");
    await expect(table.locator("tbody td")).toHaveText("John");

    // The code block starts right after the table, on the same page.
    const firstPageNumbers = pages.nth(0).locator(".hljs-ln-n");
    expect(await firstPageNumbers.count()).toBeGreaterThan(0);
    await expect(firstPageNumbers.first()).toHaveAttribute("data-line-number", "1");

    // No code line is lost across the split.
    const lines = page.locator(".hljs-ln-code");
    await expect(lines).toHaveCount(LINE_COUNT);

    // Line numbers are continuous across pages.
    const numbers = page.locator(".hljs-ln-n");
    await expect(numbers).toHaveCount(LINE_COUNT);
    for (let i = 0; i < LINE_COUNT; i++) {
        await expect(numbers.nth(i)).toHaveAttribute("data-line-number", `${i + 1}`);
    }
});
