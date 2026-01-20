import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const TOTAL_ITEMS = 9;

test("list splits correctly across pages", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(2);

    const firstPageItems = pages.nth(0).locator("li");
    const secondPageItems = pages.nth(1).locator("li");

    const firstPageCount = await firstPageItems.count();
    const secondPageCount = await secondPageItems.count();

    // First page should contain at least one item
    expect(firstPageCount).toBeGreaterThanOrEqual(1);

    // Second page should contain remaining items
    expect(secondPageCount).toBe(TOTAL_ITEMS - firstPageCount);
});

test("all list items have same bounding box", async (page) => {
    const allItems = page.locator("li");
    await expect(allItems).toHaveCount(TOTAL_ITEMS);

    const firstItemBox = await allItems.first().boundingBox();
    expect(firstItemBox).not.toBeNull();

    for (const item of await allItems.all()) {
        const itemBox = await item.boundingBox();
        expect(itemBox).not.toBeNull();
        expect(itemBox!.width).toBeCloseTo(firstItemBox!.width, 0);
        expect(itemBox!.height).toBeCloseTo(firstItemBox!.height, 0);
    }
});
