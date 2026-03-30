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

test("captions use correct margins", async (page) => {
    const figcaption = page.locator("figure figcaption").first();
    const figcaptionStyle = await evaluateComputedStyle(figcaption);

    const tableCaption = page.locator("table caption");
    const tableCaptionStyle = await evaluateComputedStyle(tableCaption);

    // Both caption types have margin (bottom captions use margin-top)
    expect(parseFloat(figcaptionStyle.marginTop)).toBeGreaterThan(0);
    expect(parseFloat(tableCaptionStyle.marginTop)).toBeGreaterThan(0);

    // Table captions have larger margin than figure captions
    expect(parseFloat(tableCaptionStyle.marginTop)).toBeGreaterThan(parseFloat(figcaptionStyle.marginTop));
});
