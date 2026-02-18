import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("asides have correct scroll CSS properties", async (page) => {
    const contentAsideFirst = page.locator(".quarkdown-docs > .content-wrapper > aside:first-child");
    const contentAsideLast = page.locator(".quarkdown-docs > .content-wrapper > aside:last-child");

    // Asides have overflow-y: auto from the SCSS
    const [asideFirstOverflowY, asideLastOverflowY] = await Promise.all([
        contentAsideFirst.evaluate((el) => getComputedStyle(el).overflowY),
        contentAsideLast.evaluate((el) => getComputedStyle(el).overflowY),
    ]);

    expect(asideFirstOverflowY).toBe("auto");
    expect(asideLastOverflowY).toBe("auto");

    // Asides are sticky positioned
    const [asideFirstPosition, asideLastPosition] = await Promise.all([
        contentAsideFirst.evaluate((el) => getComputedStyle(el).position),
        contentAsideLast.evaluate((el) => getComputedStyle(el).position),
    ]);

    expect(asideFirstPosition).toBe("sticky");
    expect(asideLastPosition).toBe("sticky");
});

test("scrolling left aside does not affect main or right aside", async (page) => {
    const contentAsideFirst = page.locator(".quarkdown-docs > .content-wrapper > aside:first-child");
    const contentAsideLast = page.locator(".quarkdown-docs > .content-wrapper > aside:last-child");

    // Get initial scroll positions
    const [initialAsideFirstScroll, initialAsideLastScroll, initialWindowScroll] = await Promise.all([
        contentAsideFirst.evaluate((el) => el.scrollTop),
        contentAsideLast.evaluate((el) => el.scrollTop),
        page.evaluate(() => window.scrollY),
    ]);

    // Scroll the left aside
    await contentAsideFirst.evaluate((el) => el.scrollBy(0, 100));

    // Verify left aside scrolled
    const asideFirstScrollAfter = await contentAsideFirst.evaluate((el) => el.scrollTop);
    expect(asideFirstScrollAfter).toBeGreaterThan(initialAsideFirstScroll);

    // Verify main (window) and right aside did not scroll
    const [asideLastScrollAfter, windowScrollAfter] = await Promise.all([
        contentAsideLast.evaluate((el) => el.scrollTop),
        page.evaluate(() => window.scrollY),
    ]);

    expect(asideLastScrollAfter).toBe(initialAsideLastScroll);
    expect(windowScrollAfter).toBe(initialWindowScroll);
});

test("scrolling window does not affect aside internal scroll", async (page) => {
    await page.emulateMedia({reducedMotion: "reduce"});

    const contentAsideFirst = page.locator(".quarkdown-docs > .content-wrapper > aside:first-child");
    const contentAsideLast = page.locator(".quarkdown-docs > .content-wrapper > aside:last-child");

    // Get initial aside scroll positions
    const [initialAsideFirstScroll, initialAsideLastScroll] = await Promise.all([
        contentAsideFirst.evaluate((el) => el.scrollTop),
        contentAsideLast.evaluate((el) => el.scrollTop),
    ]);

    // Scroll the window
    await page.evaluate(() => window.scrollBy(0, 200));

    // Verify window scrolled
    const windowScrollAfter = await page.evaluate(() => window.scrollY);
    expect(windowScrollAfter).toBeGreaterThan(0);

    // Verify aside internal scroll positions are unchanged
    const [asideFirstScrollAfter, asideLastScrollAfter] = await Promise.all([
        contentAsideFirst.evaluate((el) => el.scrollTop),
        contentAsideLast.evaluate((el) => el.scrollTop),
    ]);

    expect(asideFirstScrollAfter).toBe(initialAsideFirstScroll);
    expect(asideLastScrollAfter).toBe(initialAsideLastScroll);
});

test("anchor scroll positions heading correctly", async (page) => {
    await page.emulateMedia({reducedMotion: "reduce"});

    const header = page.locator(".quarkdown-docs > header");
    const headerBox = await header.boundingBox();
    expect(headerBox).not.toBeNull();

    // Get the aside's padding-top value
    const asidePaddingTop = await page.locator(".quarkdown-docs > .content-wrapper > aside:first-child")
        .evaluate((el) => parseFloat(getComputedStyle(el).paddingTop));

    const expectedHeadingY = headerBox!.height + asidePaddingTop;

    // Navigate to the #gamma anchor
    await page.goto(page.url() + "#gamma");

    const gammaHeading = page.locator("h1#gamma");
    await expect(gammaHeading).toBeVisible();

    const gammaBox = await gammaHeading.boundingBox();
    expect(gammaBox).not.toBeNull();

    // The heading should be positioned at header height + aside padding-top
    expect(gammaBox!.y).toBeCloseTo(expectedHeadingY, 0);
});
