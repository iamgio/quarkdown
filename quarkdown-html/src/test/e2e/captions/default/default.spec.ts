import {evaluateComputedStyle, getComputedSizeProperty} from "../../__util/css";
import {suite} from "../../quarkdown";
import {assertFigureCaption, assertTableCaption} from "../index";

const {test, expect} = suite(__dirname);

test("renders image figure caption on bottom", async (page) => {
    await assertFigureCaption(page, "img", "bottom");
});

test("renders code figure caption on bottom", async (page) => {
    await assertFigureCaption(page, "pre", "bottom");
});

test("renders table caption on bottom", async (page) => {
    await assertTableCaption(page, "bottom");
});

test("captions follow global line spacing and letter spacing", async (page) => {
    // Use a table cell as reference: it receives the same line-height and letter-spacing rules
    const td = page.locator("td").first();
    const refStyle = await evaluateComputedStyle(td);

    for (const caption of [
        page.locator("figure figcaption").first(),
        page.locator("table caption"),
    ]) {
        await expect(caption).toHaveCSS("line-height", refStyle.lineHeight);
        await expect(caption).toHaveCSS("letter-spacing", refStyle.letterSpacing);
    }
});

test("applies correct caption margins", async (page) => {
    const figcaption = page.locator("figure figcaption").first();
    const figcaptionStyle = await evaluateComputedStyle(figcaption);
    const figCaptionMargin = await getComputedSizeProperty(figcaption, "var(--qd-caption-margin)");
    expect(parseFloat(figcaptionStyle.marginTop)).toBeCloseTo(figCaptionMargin, 1);

    const tableCaption = page.locator("table caption");
    const tableCaptionStyle = await evaluateComputedStyle(tableCaption);
    const tableCaptionMargin = await getComputedSizeProperty(tableCaption, "var(--caption-margin)");
    expect(parseFloat(tableCaptionStyle.marginTop)).toBeCloseTo(tableCaptionMargin, 1);

    // Table captions have larger margin than figure captions
    expect(parseFloat(tableCaptionStyle.marginTop)).toBeGreaterThan(parseFloat(figcaptionStyle.marginTop));
});
