import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("shows a bottom caption once, on the last portion of a split table", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(2);

    // The caption appears exactly once in the whole document.
    const caption = page.locator("table caption");
    await expect(caption).toHaveCount(1);
    await expect(caption).toContainText("My caption");

    // A bottom caption belongs to the last portion of the table.
    await expect(pages.nth(1).locator("table caption")).toHaveCount(1);
    await expect(caption).toHaveCSS("caption-side", "bottom");
});
