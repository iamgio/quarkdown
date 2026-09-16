import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

// Without line numbers, the code block is split in the middle of its raw text:
// the indentation of the line cut by the page break is restored on the new page.
test("preserves indentation across a plain code block split", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(2);

    const firstPortion = await pages.nth(0).locator("pre code").textContent();
    const secondPortion = await pages.nth(1).locator("pre code").textContent();

    expect(firstPortion).toContain("Line 1");
    expect(firstPortion).toContain("    Line 2");

    // Lines 3 and 4 keep their 8- and 12-space indentation.
    expect(secondPortion).toMatch(/^\s*? {8}Line 3\n {12}Line 4/);
});
