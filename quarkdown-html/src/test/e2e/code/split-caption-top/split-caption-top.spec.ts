import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const LINE_COUNT = 20;

test("shows a top caption once, on the first portion of a split code block", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(3);

    // The code block is split across all pages, with no line lost.
    await expect(page.locator(".hljs-ln-code")).toHaveCount(LINE_COUNT);
    for (let i = 0; i < 3; i++) {
        expect(await pages.nth(i).locator(".hljs-ln-code").count()).toBeGreaterThan(0);
    }

    // The caption appears exactly once in the whole document.
    const caption = page.locator("figure > figcaption");
    await expect(caption).toHaveCount(1);
    await expect(caption).toContainText("My caption");

    // A top caption belongs to the first portion of the code block, along with its content.
    await expect(pages.nth(0).locator("figure > figcaption")).toHaveCount(1);
});
