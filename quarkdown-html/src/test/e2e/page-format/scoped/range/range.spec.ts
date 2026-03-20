import {suite} from "../../../quarkdown";

const {test, expect} = suite(__dirname);

// The source defines:
// - Global: A4, bordercolor:red
// - Range 2..3: margin:3cm, bordercolor:green

function getPage(page: import("@playwright/test").Page, index: number) {
    return page.locator(".pagedjs_page").nth(index);
}

test("page outside range has global border color", async (page) => {
    const firstPage = getPage(page, 0);
    const area = firstPage.locator(".pagedjs_area");

    await expect(area).toHaveCSS("border-left-color", "rgb(255, 0, 0)");
});

test("page inside range has scoped border color", async (page) => {
    // Page index 1 = page number 2 (in range 2..3).
    const secondPage = getPage(page, 1);
    const area = secondPage.locator(".pagedjs_area");

    await expect(area).toHaveCSS("border-left-color", "rgb(0, 128, 0)");
});

test("third page also has scoped border color", async (page) => {
    // Page index 2 = page number 3 (in range 2..3).
    const thirdPage = getPage(page, 2);
    const area = thirdPage.locator(".pagedjs_area");

    await expect(area).toHaveCSS("border-left-color", "rgb(0, 128, 0)");
});

test("page after range reverts to global border color", async (page) => {
    // Page index 3 = page number 4 (outside range 2..3).
    const fourthPage = getPage(page, 3);
    const area = fourthPage.locator(".pagedjs_area");

    await expect(area).toHaveCSS("border-left-color", "rgb(255, 0, 0)");
});

test("page inside range has larger margin", async (page) => {
    const firstPage = getPage(page, 0);
    const secondPage = getPage(page, 1);

    const firstMargin = firstPage.locator(".pagedjs_margin-left");
    const secondMargin = secondPage.locator(".pagedjs_margin-left");

    const firstBox = await firstMargin.boundingBox();
    const secondBox = await secondMargin.boundingBox();
    expect(firstBox).not.toBeNull();
    expect(secondBox).not.toBeNull();

    // Range pages (2..3) have 3cm margin, which is larger than the default.
    expect(secondBox!.width).toBeGreaterThan(firstBox!.width);
});
