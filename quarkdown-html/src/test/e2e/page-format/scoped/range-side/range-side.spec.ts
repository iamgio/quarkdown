import {suite} from "../../../quarkdown";

const {test, expect} = suite(__dirname);

// The source layers four scopes:
// 1. Global:                   borderleft:1cm, bordercolor:red
// 2. side:left:                bordercolor:blue
// 3. range:2..4:               bordercolor:green
// 4. side:right + range:2..4:  bordercolor:purple
//
// Page 1 is right (recto), page 2 is left, page 3 is right, page 4 is left, page 5 is right.
//
// Expected border-left-color per page:
//   Page 1 (right, outside range):  red    (global)
//   Page 2 (left, in range):        green  (range overrides side)
//   Page 3 (right, in range):       purple (side+range)
//   Page 4 (left, in range):        green  (range, not right so side+range doesn't apply)
//   Page 5 (right, outside range):  red    (global)

function getPage(page: import("@playwright/test").Page, index: number) {
    return page.locator(".pagedjs_page").nth(index);
}

function getArea(page: import("@playwright/test").Page, index: number) {
    return getPage(page, index).locator(".pagedjs_area");
}

test("page 1 (right, outside range) has global red border", async (page) => {
    await expect(getArea(page, 0)).toHaveCSS("border-left-color", "rgb(255, 0, 0)");
});

test("page 2 (left, in range) has range green border", async (page) => {
    await expect(getArea(page, 1)).toHaveCSS("border-left-color", "rgb(0, 128, 0)");
});

test("page 3 (right, in range) has side+range purple border", async (page) => {
    await expect(getArea(page, 2)).toHaveCSS("border-left-color", "rgb(128, 0, 128)");
});

test("page 4 (left, in range) has range green border", async (page) => {
    await expect(getArea(page, 3)).toHaveCSS("border-left-color", "rgb(0, 128, 0)");
});

test("page 5 (right, outside range) reverts to global red border", async (page) => {
    await expect(getArea(page, 4)).toHaveCSS("border-left-color", "rgb(255, 0, 0)");
});
