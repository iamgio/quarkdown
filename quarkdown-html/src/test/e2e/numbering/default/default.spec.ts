import {getBeforeContent, isBeforeInline} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies numbering to all element types", async (page) => {
    // Headings: 1.1 format (h1=1, h2=1.1, h3 not numbered)
    const h1 = page.locator("h1").first();
    expect(await getBeforeContent(h1)).toContain("1");
    expect(await isBeforeInline(h1)).toBe(true);
    expect(await getBeforeContent(page.locator("h2").first())).toContain("1.1");

    // Figures: 1.a format
    const figures = page.locator("figure[id^='figure-'] figcaption");
    await expect(figures).toHaveCount(4);
    expect(await getBeforeContent(figures.nth(0))).toContain("0.a");
    expect(await isBeforeInline(figures.nth(0))).toBe(true);
    expect(await getBeforeContent(figures.nth(1))).toContain("1.a");
    expect(await getBeforeContent(figures.nth(2))).toContain("1.b");
    expect(await getBeforeContent(figures.nth(3))).toContain("1.c");

    // Code: 1 format
    const codeBlocks = page.locator("figure[id^='listing-'] figcaption");
    await expect(codeBlocks).toHaveCount(2);
    expect(await getBeforeContent(codeBlocks.nth(0))).toContain("1");
    expect(await getBeforeContent(codeBlocks.nth(1))).toContain("2");

    // Equations: (1.A) format
    const equations = page.locator("formula[data-block]");
    await expect(equations.nth(0)).toHaveAttribute("data-location", "(1.A)");
    await expect(equations.nth(1)).toHaveAttribute("data-location", "(1.B)");

    // Tables: i format
    const tableCaption = page.locator("table caption");
    expect(await getBeforeContent(tableCaption)).toContain("i");

    // Footnotes: I format
    const footnoteRef = page.locator(".footnote-reference a");
    await expect(footnoteRef).toHaveText("I");
});
