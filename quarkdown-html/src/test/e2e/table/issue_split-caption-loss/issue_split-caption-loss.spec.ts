import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const ROW_COUNT = 78;

// The caption of a numbered table (emitted even without a caption text) used to be
// placed at the end of the table in the DOM: when the table split across pages,
// the caption landed in the last portion, and moving it back to the first portion
// after pagination pushed the portion's bottom rows into a hidden overflow column.
test("splitting a numbered table with a top caption loses no rows", async (page) => {
    const pages = page.locator(".pagedjs_page");
    expect(await pages.count()).toBeGreaterThan(1);

    // Every row is present.
    const rows = page.locator(".pagedjs_page table > tbody > tr");
    await expect(rows).toHaveCount(ROW_COUNT);

    // The numbered caption appears once, on the first portion of the table.
    const caption = page.locator("table > caption");
    await expect(caption).toHaveCount(1);
    await expect(page.locator("table[data-split-to] > caption")).toHaveCount(1);

    // Every row lies within its page's content area, not in an invisible overflow column.
    for (const row of await rows.all()) {
        const rowBox = await row.boundingBox();
        const areaBox = await row.locator("xpath=ancestor::*[contains(@class, 'pagedjs_area')]").boundingBox();
        expect(rowBox).not.toBeNull();
        expect(areaBox).not.toBeNull();
        expect(rowBox!.x).toBeGreaterThanOrEqual(areaBox!.x - 1);
        expect(rowBox!.x + rowBox!.width).toBeLessThanOrEqual(areaBox!.x + areaBox!.width + 1);
    }
});
