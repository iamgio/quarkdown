import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const ROW_COUNT = 25;

// The repeated header used to be cloned after pagination: its height pushed
// the bottom rows of each split portion into a hidden overflow column, cutting them off.
test("repeating a tall header does not push out the bottom rows", async (page) => {
    const pages = page.locator(".pagedjs_page");
    const pageCount = await pages.count();
    expect(pageCount).toBeGreaterThan(1);

    // Every row is present, in order, and each portion repeats the header.
    const rows = page.locator(".pagedjs_page table > tbody > tr");
    await expect(rows).toHaveCount(ROW_COUNT);
    await expect(rows.first().locator("td").first()).toHaveText("R1");
    await expect(rows.last().locator("td").first()).toHaveText(`R${ROW_COUNT}`);

    for (let i = 0; i < pageCount; i++) {
        await expect(pages.nth(i).locator("table > thead")).toHaveCount(1);
    }

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
