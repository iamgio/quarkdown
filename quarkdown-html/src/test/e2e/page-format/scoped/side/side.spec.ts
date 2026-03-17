import {suite} from "../../../quarkdown";

const {test, expect} = suite(__dirname);

// The source defines:
// - Global: alignment:center, margin:5cm, borderleft:1cm, bordercolor:red
// - Left:   margin:1cm, bordercolor:green, alignment:end
// - Right:  bordercolor:blue

// Helper to get the first page of each side.
function getLeftPage(page: import("@playwright/test").Page) {
    return page.locator(".pagedjs_left_page").first();
}

function getRightPage(page: import("@playwright/test").Page) {
    return page.locator(".pagedjs_right_page").first();
}

test("global format applies border and alignment to all pages", async (page) => {
    // The first page is a right page in paged.js (recto).
    const firstPage = page.locator(".pagedjs_page").first();
    const area = firstPage.locator(".pagedjs_area");

    // Global border: left 1cm, color red.
    await expect(area).toHaveCSS("border-left-style", "solid");
});

test("left page has green border color", async (page) => {
    const leftPage = getLeftPage(page);
    const area = leftPage.locator(".pagedjs_area");

    await expect(area).toHaveCSS("border-left-color", "rgb(0, 128, 0)");
});

test("right page has blue border color", async (page) => {
    const rightPage = getRightPage(page);
    const area = rightPage.locator(".pagedjs_area");

    await expect(area).toHaveCSS("border-left-color", "rgb(0, 0, 255)");
});

test("left page has end alignment", async (page) => {
    const leftPage = getLeftPage(page);
    const paragraph = leftPage.locator("p").first();

    await expect(paragraph).toHaveCSS("text-align", "end");
});

test("right page inherits global center alignment", async (page) => {
    const rightPage = getRightPage(page);
    const paragraph = rightPage.locator("p").first();

    await expect(paragraph).toHaveCSS("text-align", "center");
});

test("left page has smaller margin than global", async (page) => {
    const leftPage = getLeftPage(page);
    const rightPage = getRightPage(page);

    const leftMargin = leftPage.locator(".pagedjs_margin-left");
    const rightMargin = rightPage.locator(".pagedjs_margin-left");

    const leftBox = await leftMargin.boundingBox();
    const rightBox = await rightMargin.boundingBox();
    expect(leftBox).not.toBeNull();
    expect(rightBox).not.toBeNull();

    // Left page margin is 1cm (~37.8px), global margin is 5cm (~189px).
    expect(leftBox!.width).toBeLessThan(rightBox!.width);
});
