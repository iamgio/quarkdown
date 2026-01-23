import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const NAV_SELECTOR = 'nav[data-role="page-list"]';
const BUTTON_AREA_SELECTOR = "#sibling-pages-button-area";
const PREVIOUS_LINK_SELECTOR = "#previous-page-anchor";
const NEXT_LINK_SELECTOR = "#next-page-anchor";

async function assertButtons(
    page: any,
    expect: any,
    hasPrevious: boolean,
    hasNext: boolean
) {
    const buttonArea = page.locator(BUTTON_AREA_SELECTOR);
    const previousLink = page.locator(PREVIOUS_LINK_SELECTOR);
    const nextLink = page.locator(NEXT_LINK_SELECTOR);
    const main = page.locator(".content-wrapper > main");

    // Navigation with page list should exist
    await expect(page.locator(NAV_SELECTOR)).toBeAttached();

    // Button area should exist
    await expect(buttonArea).toBeAttached();

    if (hasPrevious) {
        await expect(previousLink).toBeAttached();
    } else {
        await expect(previousLink).not.toBeAttached();
    }

    if (hasNext) {
        await expect(nextLink).toBeAttached();
    } else {
        await expect(nextLink).not.toBeAttached();
    }

    // Layout assertions
    const mainBox = await main.boundingBox();
    expect(mainBox).not.toBeNull();

    if (hasPrevious && hasNext) {
        // Two buttons: each takes 50% width
        const previousBox = await previousLink.boundingBox();
        const nextBox = await nextLink.boundingBox();
        expect(previousBox).not.toBeNull();
        expect(nextBox).not.toBeNull();

        const halfWidth = mainBox!.width / 2;
        expect(previousBox!.width).toBeCloseTo(halfWidth, -2);
        expect(nextBox!.width).toBeCloseTo(halfWidth, -2);
        expect(previousBox!.x).toBeLessThan(nextBox!.x);
    } else if (hasPrevious) {
        // Only previous: takes 100% width
        const previousBox = await previousLink.boundingBox();
        expect(previousBox).not.toBeNull();
        expect(previousBox!.width).toBeCloseTo(mainBox!.width, -1);
    } else if (hasNext) {
        // Only next: takes 100% width
        const nextBox = await nextLink.boundingBox();
        expect(nextBox).not.toBeNull();
        expect(nextBox!.width).toBeCloseTo(mainBox!.width, -1);
    }
}

test("first page has only next button", async (page) => {
    await assertButtons(page, expect, false, true);
}, {subpath: "page-1"});

test("middle page has both buttons", async (page) => {
    await assertButtons(page, expect, true, true);
}, {subpath: "page-2"});

test("last page has only previous button", async (page) => {
    await assertButtons(page, expect, true, false);
}, {subpath: "page-3"});
