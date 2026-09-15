import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const LINE_COUNT = 49;

// A taller-than-page code block after a heading used to be lost entirely:
// the paged engine could neither fit it below the heading nor split it in place,
// leaving an empty first page, the heading alone, and the code in a hidden overflow column.
test("splits a taller-than-page code block right after its heading", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(2);

    // The heading stays on the first page, with the code starting right below it.
    await expect(pages.nth(0).locator("h1")).toHaveText("Chapter");
    const firstPageNumbers = pages.nth(0).locator(".hljs-ln-n");
    expect(await firstPageNumbers.count()).toBeGreaterThan(0);
    await expect(firstPageNumbers.first()).toHaveAttribute("data-line-number", "1");

    // Every code line is rendered across the pages.
    await expect(page.locator(".hljs-ln-code")).toHaveCount(LINE_COUNT);
    expect(await pages.nth(1).locator(".hljs-ln-code").count()).toBeGreaterThan(0);

    // Each portion lies within its page's content area, not in an invisible overflow column.
    for (const pre of await page.locator(".pagedjs_page pre").all()) {
        const preBox = await pre.boundingBox();
        const areaBox = await pre.locator("xpath=ancestor::*[contains(@class, 'pagedjs_area')]").boundingBox();
        expect(preBox).not.toBeNull();
        expect(areaBox).not.toBeNull();
        expect(preBox!.x).toBeGreaterThanOrEqual(areaBox!.x - 1);
        expect(preBox!.x + preBox!.width).toBeLessThanOrEqual(areaBox!.x + areaBox!.width + 1);
    }
});
