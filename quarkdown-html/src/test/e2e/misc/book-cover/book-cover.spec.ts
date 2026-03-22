import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("renders a full-bleed cover page", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(2);

    const firstPage = pages.nth(0);
    const secondPage = pages.nth(1);

    const firstPageBox = await firstPage.boundingBox();
    const firstAreaBox = await firstPage.locator(".pagedjs_area").boundingBox();
    const secondPageBox = await secondPage.boundingBox();
    const secondAreaBox = await secondPage.locator(".pagedjs_area").boundingBox();

    expect(firstPageBox).not.toBeNull();
    expect(firstAreaBox).not.toBeNull();
    expect(secondPageBox).not.toBeNull();
    expect(secondAreaBox).not.toBeNull();

    // The cover page has zero margins, so the page and area sizes match.
    expect(firstPageBox!.width).toBeCloseTo(firstAreaBox!.width, 0);
    expect(firstPageBox!.height).toBeCloseTo(firstAreaBox!.height, 0);

    // The second page has default margins, so the area is smaller than the page.
    expect(secondPageBox!.width).toBeGreaterThan(secondAreaBox!.width);
    expect(secondPageBox!.height).toBeGreaterThan(secondAreaBox!.height);

    // The cover image fills the entire page.
    const img = firstPage.locator("img");
    const imgBox = await img.boundingBox();
    expect(imgBox).not.toBeNull();
    expect(imgBox!.width).toBeCloseTo(firstPageBox!.width, 0);
    expect(imgBox!.height).toBeCloseTo(firstPageBox!.height, 0);

    // The heading is on the second page, not the cover.
    await expect(firstPage.locator("h1")).toHaveCount(0);
    await expect(secondPage.locator("h1")).toHaveCount(1);
});
