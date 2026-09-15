import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const BLOCK_COUNT = 4;
const LINES_PER_BLOCK = 10;

// Code blocks are highlighted before pagination, so that page breaks account for their
// final height. When highlighting ran after pagination, it inflated the blocks and pushed
// content near page breaks into paged.js's hidden overflow column, cutting it off.
test("renders every code line within page bounds", async (page) => {
    const lines = page.locator(".pagedjs_page .hljs-ln-code");
    await expect(lines).toHaveCount(BLOCK_COUNT * LINES_PER_BLOCK);

    for (const pre of await page.locator(".pagedjs_page pre").all()) {
        const preBox = await pre.boundingBox();
        const areaBox = await pre.locator("xpath=ancestor::*[contains(@class, 'pagedjs_area')]").boundingBox();
        expect(preBox).not.toBeNull();
        expect(areaBox).not.toBeNull();

        // The block lies within its page's content area, not in an invisible overflow column.
        expect(preBox!.x).toBeGreaterThanOrEqual(areaBox!.x - 1);
        expect(preBox!.x + preBox!.width).toBeLessThanOrEqual(areaBox!.x + areaBox!.width + 1);
    }
});
