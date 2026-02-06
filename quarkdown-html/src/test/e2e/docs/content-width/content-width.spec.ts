import {suite} from "../../quarkdown";

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
