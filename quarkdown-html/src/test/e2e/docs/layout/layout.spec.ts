import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

/** Asserts two values are approximately equal within 2px tolerance (accounts for scrollbar and sub-pixel rendering). */
function expectApprox(actual: number, expected: number) {
    expect(Math.abs(actual - expected)).toBeLessThanOrEqual(2);
}

test("header and content-wrapper have full viewport width", async (page) => {
    const viewportSize = page.viewportSize()!;

    const header = page.locator(".quarkdown-docs > header");
    const contentWrapper = page.locator(".quarkdown-docs > .content-wrapper");

    const headerBox = await header.boundingBox();
    const contentWrapperBox = await contentWrapper.boundingBox();

    expect(headerBox).not.toBeNull();
    expect(contentWrapperBox).not.toBeNull();

    expect(headerBox!.width).toBe(viewportSize.width);
    expect(contentWrapperBox!.width).toBe(viewportSize.width);
});

test("header starts at y=0 and ends where content-wrapper starts", async (page) => {
    const header = page.locator(".quarkdown-docs > header");
    const contentWrapper = page.locator(".quarkdown-docs > .content-wrapper");

    const headerBox = await header.boundingBox();
    const contentWrapperBox = await contentWrapper.boundingBox();

    expect(headerBox).not.toBeNull();
    expect(contentWrapperBox).not.toBeNull();

    expect(headerBox!.y).toBe(0);
    expect(headerBox!.y + headerBox!.height).toBeCloseTo(contentWrapperBox!.y, 0);
});

test("content-wrapper extends to viewport height", async (page) => {
    const viewportSize = page.viewportSize()!;
    const contentWrapper = page.locator(".quarkdown-docs > .content-wrapper");

    const contentWrapperBox = await contentWrapper.boundingBox();
    expect(contentWrapperBox).not.toBeNull();

    // Content wrapper should extend to at least the viewport height
    // (it may be taller if content overflows)
    expect(contentWrapperBox!.y + contentWrapperBox!.height).toBeGreaterThanOrEqual(viewportSize.height);
});

test("header and content-wrapper columns are aligned", async (page) => {
    const headerAsideFirst = page.locator(".quarkdown-docs > header > aside:first-child");
    const headerMain = page.locator(".quarkdown-docs > header > main");
    const headerAsideLast = page.locator(".quarkdown-docs > header > aside:last-child");

    const contentAsideFirst = page.locator(".quarkdown-docs > .content-wrapper > aside:first-child");
    const contentMain = page.locator(".quarkdown-docs > .content-wrapper > main");
    const contentAsideLast = page.locator(".quarkdown-docs > .content-wrapper > aside:last-child");

    const [
        headerAsideFirstBox,
        headerMainBox,
        headerAsideLastBox,
        contentAsideFirstBox,
        contentMainBox,
        contentAsideLastBox,
    ] = await Promise.all([
        headerAsideFirst.boundingBox(),
        headerMain.boundingBox(),
        headerAsideLast.boundingBox(),
        contentAsideFirst.boundingBox(),
        contentMain.boundingBox(),
        contentAsideLast.boundingBox(),
    ]);

    expect(headerAsideFirstBox).not.toBeNull();
    expect(headerMainBox).not.toBeNull();
    expect(headerAsideLastBox).not.toBeNull();
    expect(contentAsideFirstBox).not.toBeNull();
    expect(contentMainBox).not.toBeNull();
    expect(contentAsideLastBox).not.toBeNull();

    // First aside (left sidebar): same x and width
    expectApprox(headerAsideFirstBox!.x, contentAsideFirstBox!.x);
    expectApprox(headerAsideFirstBox!.width, contentAsideFirstBox!.width);

    // Main content area: same x and width
    expectApprox(headerMainBox!.x, contentMainBox!.x);
    expectApprox(headerMainBox!.width, contentMainBox!.width);

    // Last aside (right sidebar): same x and width
    expectApprox(headerAsideLastBox!.x, contentAsideLastBox!.x);
    expectApprox(headerAsideLastBox!.width, contentAsideLastBox!.width);
});

test("search field aligns with main content first child", async (page) => {
    const searchField = page.locator(".quarkdown-docs > header > main .search-field");
    const mainFirstChild = page.locator(".quarkdown-docs > .content-wrapper > main > :first-child");

    const searchFieldBox = await searchField.boundingBox();
    const mainFirstChildBox = await mainFirstChild.boundingBox();

    expect(searchFieldBox).not.toBeNull();
    expect(mainFirstChildBox).not.toBeNull();

    expectApprox(searchFieldBox!.x, mainFirstChildBox!.x);
});
