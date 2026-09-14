import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const LINE_COUNT = 20;

test("shows a bottom caption once, on the last portion of a split code block", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(2);

    // The code block is split across both pages, with no line lost.
    await expect(page.locator(".hljs-ln-code")).toHaveCount(LINE_COUNT);
    expect(await pages.nth(0).locator(".hljs-ln-code").count()).toBeGreaterThan(0);
    expect(await pages.nth(1).locator(".hljs-ln-code").count()).toBeGreaterThan(0);

    // The caption appears exactly once in the whole document.
    const caption = page.locator("figure > figcaption");
    await expect(caption).toHaveCount(1);
    await expect(caption).toContainText("My caption");

    // A bottom caption belongs to the last portion of the code block.
    await expect(pages.nth(1).locator("figure > figcaption")).toHaveCount(1);
});
