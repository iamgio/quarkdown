import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const PAGE_COUNT = 2;
const ROW_COUNT = 10;

test("repeats the header row of a table split across pages", async (page) => {
    // The table splits across exactly two pages.
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(PAGE_COUNT);

    // All rows are preserved across the split, in order.
    const rows = page.locator(".pagedjs_page table > tbody > tr");
    await expect(rows).toHaveCount(ROW_COUNT);
    await expect(rows.first().locator("td").first()).toHaveText("A");
    await expect(rows.last().locator("td").first()).toHaveText("J");

    // Each page has one portion of the table, with the header row repeated above it.
    for (let i = 0; i < PAGE_COUNT; i++) {
        const header = pages.nth(i).locator("table > thead");
        await expect(header).toHaveCount(1);
        await expect(header.locator("th").first()).toHaveText("Name");
    }
});
