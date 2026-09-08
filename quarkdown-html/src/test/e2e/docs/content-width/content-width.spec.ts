import {test as base} from "@playwright/test";
import {suite} from "../../quarkdown";

// Tests in this file compile the same document to the same output directory,
// so they must not run in parallel.
base.describe.configure({mode: "default"});

const {test, expect} = suite(__dirname);

interface LayoutWidths {
    headerAsideFirst: number;
    headerMain: number;
    headerAsideLast: number;
    contentAsideFirst: number;
    contentMain: number;
    contentAsideLast: number;
}

async function getLayoutWidths(page: any): Promise<LayoutWidths> {
    const [
        headerAsideFirstBox,
        headerMainBox,
        headerAsideLastBox,
        contentAsideFirstBox,
        contentMainBox,
        contentAsideLastBox,
    ] = await Promise.all([
        page.locator(".quarkdown-docs > header > aside:first-child").boundingBox(),
        page.locator(".quarkdown-docs > header > main").boundingBox(),
        page.locator(".quarkdown-docs > header > aside:last-child").boundingBox(),
        page.locator(".quarkdown-docs > .content-wrapper > aside:first-child").boundingBox(),
        page.locator(".quarkdown-docs > .content-wrapper > main").boundingBox(),
        page.locator(".quarkdown-docs > .content-wrapper > aside:last-child").boundingBox(),
    ]);

    return {
        headerAsideFirst: headerAsideFirstBox!.width,
        headerMain: headerMainBox!.width,
        headerAsideLast: headerAsideLastBox!.width,
        contentAsideFirst: contentAsideFirstBox!.width,
        contentMain: contentMainBox!.width,
        contentAsideLast: contentAsideLastBox!.width,
    };
}

test("layout widths match between simple content and long code block", async (page) => {
    // Get widths from simple page
    const simpleWidths = await getLayoutWidths(page);

    // Navigate to long-code page
    await page.goto(page.url().replace("/simple/", "/long-code/"));
    await page.waitForFunction(() => (window as any).isReady());

    // Get widths from long-code page
    const longCodeWidths = await getLayoutWidths(page);

    // Assert all widths match
    expect(longCodeWidths.headerAsideFirst).toBe(simpleWidths.headerAsideFirst);
    expect(longCodeWidths.headerMain).toBe(simpleWidths.headerMain);
    expect(longCodeWidths.headerAsideLast).toBe(simpleWidths.headerAsideLast);
    expect(longCodeWidths.contentAsideFirst).toBe(simpleWidths.contentAsideFirst);
    expect(longCodeWidths.contentMain).toBe(simpleWidths.contentMain);
    expect(longCodeWidths.contentAsideLast).toBe(simpleWidths.contentAsideLast);
}, {subpath: "simple"});

test("wide table scrolls within the content area without affecting layout", async (page) => {
    // Get widths from simple page
    const simpleWidths = await getLayoutWidths(page);

    // Navigate to wide-table page
    await page.goto(page.url().replace("/simple/", "/wide-table/"));
    await page.waitForFunction(() => (window as any).isReady());

    // The wide table does not alter the overall layout.
    const wideTableWidths = await getLayoutWidths(page);
    expect(wideTableWidths.contentMain).toBe(simpleWidths.contentMain);
    expect(wideTableWidths.contentAsideFirst).toBe(simpleWidths.contentAsideFirst);
    expect(wideTableWidths.contentAsideLast).toBe(simpleWidths.contentAsideLast);

    // The document itself is not horizontally scrollable.
    const documentOverflow = await page.evaluate(
        () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
    );
    expect(documentOverflow).toBe(0);

    // The table scrolls within its own wrapper.
    const scrollArea = page.locator(".quarkdown-docs > .content-wrapper > main .table-scroll-area");
    await expect(scrollArea).toHaveCSS("overflow-x", "auto");
    expect(await scrollArea.evaluate((el: Element) => el.scrollWidth > el.clientWidth)).toBe(true);

    // The scroll area does not overlap the right sidebar.
    const scrollAreaBox = await scrollArea.boundingBox();
    const asideBox = await page.locator(".quarkdown-docs > .content-wrapper > aside:last-child").boundingBox();
    expect(scrollAreaBox!.x + scrollAreaBox!.width).toBeLessThanOrEqual(asideBox!.x);
}, {subpath: "simple"});

test("regular table is unaffected by the scroll wrapper", async (page) => {
    const scrollArea = page.locator(".quarkdown-docs > .content-wrapper > main .table-scroll-area");
    await expect(scrollArea).toHaveCount(1);

    // The wrapper has nothing to scroll.
    expect(await scrollArea.evaluate((el: Element) => el.scrollWidth <= el.clientWidth)).toBe(true);

    // The document itself is not horizontally scrollable.
    const documentOverflow = await page.evaluate(
        () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
    );
    expect(documentOverflow).toBe(0);

    const tableBox = await scrollArea.locator("table").boundingBox();
    const mainBox = await page.locator(".quarkdown-docs > .content-wrapper > main").boundingBox();

    // The table keeps its natural shrink-to-fit width.
    expect(tableBox!.width).toBeLessThan(mainBox!.width);

    // The table stays horizontally centered within the content area,
    // as the default layout theme centers tables.
    const leftGap = tableBox!.x - mainBox!.x;
    const rightGap = mainBox!.x + mainBox!.width - (tableBox!.x + tableBox!.width);
    expect(Math.abs(leftGap - rightGap)).toBeLessThanOrEqual(2);
}, {subpath: "regular-table"});
