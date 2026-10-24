import {suite} from "../../../quarkdown";

const {test, expect} = suite(__dirname);

const PARAGRAPH_COUNT = 60;

// Padding shrinks the content box of each page. Pagination must account for it,
// otherwise content near the bottom of a page would be pushed into the padding,
// or into paged.js's hidden overflow column, and cut off.
test("padding does not cut content off pages", async (page) => {
    const pages = page.locator(".pagedjs_page");
    expect(await pages.count()).toBeGreaterThan(1);

    const paragraphs = page.locator(".pagedjs_page p");
    await expect(paragraphs).toHaveCount(PARAGRAPH_COUNT);

    for (const paragraph of await paragraphs.all()) {
        const paragraphBox = await paragraph.boundingBox();
        const contentBox = await paragraph
            .locator("xpath=ancestor::*[contains(@class, 'pagedjs_page_content')]")
            .boundingBox();
        expect(paragraphBox).not.toBeNull();
        expect(contentBox).not.toBeNull();

        // The paragraph lies within the padded content box of its page.
        expect(paragraphBox!.x).toBeGreaterThanOrEqual(contentBox!.x - 1);
        expect(paragraphBox!.x + paragraphBox!.width).toBeLessThanOrEqual(contentBox!.x + contentBox!.width + 1);
        expect(paragraphBox!.y).toBeGreaterThanOrEqual(contentBox!.y - 1);
        expect(paragraphBox!.y + paragraphBox!.height).toBeLessThanOrEqual(contentBox!.y + contentBox!.height + 1);
    }
});
