import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("pushes a trailing heading to the next page", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(2);

    const firstPage = pages.nth(0);
    const secondPage = pages.nth(1);

    // The heading is on the second page, not stranded at the bottom of the first.
    await expect(firstPage.locator("h2")).toHaveCount(0);
    await expect(secondPage.locator("h2")).toHaveCount(1);

    // The first page is not full: it has enough vertical space to fit the heading,
    // confirming the push was caused by `break-after: avoid` rather than a natural overflow.
    const firstPageContentBox = await firstPage.locator(".pagedjs_page_content").boundingBox();
    const firstPageParagraphBox = await firstPage.locator("p").boundingBox();
    const headingBox = await secondPage.locator("h2").boundingBox();

    expect(firstPageContentBox).not.toBeNull();
    expect(firstPageParagraphBox).not.toBeNull();
    expect(headingBox).not.toBeNull();

    const remainingSpace =
        firstPageContentBox!.y + firstPageContentBox!.height - (firstPageParagraphBox!.y + firstPageParagraphBox!.height);
    expect(remainingSpace).toBeGreaterThanOrEqual(headingBox!.height);
});
