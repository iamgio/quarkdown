import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const LINE_COUNT = 40;

test("a formula taller than a page wraps across pages", async (page) => {
    const pages = page.locator(".pagedjs_page");
    const pageCount = await pages.count();
    expect(pageCount).toBeGreaterThan(1);

    // The formula is split: each page shows a portion of it.
    await expect(page.locator(".pagedjs_page formula")).toHaveCount(pageCount);

    // Every line is rendered exactly once: each one holds a summation.
    await expect(page.locator(".pagedjs_page formula .op-limits")).toHaveCount(LINE_COUNT);

    // Every line lies within its page's content area, not in an invisible overflow column.
    for (const line of await page.locator(".pagedjs_page formula .katex-html > .base").all()) {
        const lineBox = await line.boundingBox();
        const areaBox = await line.locator("xpath=ancestor::*[contains(@class, 'pagedjs_area')]").boundingBox();
        expect(lineBox).not.toBeNull();
        expect(areaBox).not.toBeNull();
        expect(lineBox!.x).toBeGreaterThanOrEqual(areaBox!.x - 1);
        expect(lineBox!.x + lineBox!.width).toBeLessThanOrEqual(areaBox!.x + areaBox!.width + 1);
        expect(lineBox!.y).toBeGreaterThanOrEqual(areaBox!.y - 1);
        expect(lineBox!.y + lineBox!.height).toBeLessThanOrEqual(areaBox!.y + areaBox!.height + 1);
    }
});
