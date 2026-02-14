import {evaluateComputedStyle, getComputedSizeProperty} from "../../../__util/css";
import {suite} from "../../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies correct spacing with containers", async (page) => {
    const paragraphMargin = await getComputedSizeProperty(page, "var(--qd-paragraph-vertical-margin)");
    const topParagraphs = page.locator("main > p");
    const containers = page.locator("main > .container");
    await expect(topParagraphs).toHaveCount(2);
    await expect(containers).toHaveCount(6);

    // Paragraph 1 has no margins (first element)
    const p1Style = await evaluateComputedStyle(topParagraphs.nth(0));
    expect(p1Style.margin).toBe("0px");

    // Paragraph 2 has only margin-top (preceded by container with p:last-child)
    const p2Style = await evaluateComputedStyle(topParagraphs.nth(1));
    expect(parseFloat(p2Style.marginTop)).toBeCloseTo(paragraphMargin, 1);
    expect(p2Style.marginBottom).toBe("0px");

    // Containers 1, 2, 4 have margin-top, but their inner paragraphs have no margins
    for (const i of [0, 1, 3]) {
        const cStyle = await evaluateComputedStyle(containers.nth(i));
        expect(parseFloat(cStyle.marginTop)).toBeCloseTo(paragraphMargin, 1);
        expect(cStyle.marginBottom).toBe("0px");

        const innerPStyle = await evaluateComputedStyle(containers.nth(i).locator("p").first());
        expect(innerPStyle.margin).toBe("0px");
    }

    // Container 3 has margin-top; first inner paragraph has no margins, second has margin-top
    const c3Style = await evaluateComputedStyle(containers.nth(2));
    expect(parseFloat(c3Style.marginTop)).toBeCloseTo(paragraphMargin, 1);
    expect(c3Style.marginBottom).toBe("0px");

    const c3Paragraphs = containers.nth(2).locator("p");
    await expect(c3Paragraphs).toHaveCount(2);

    const c3P1Style = await evaluateComputedStyle(c3Paragraphs.nth(0));
    expect(c3P1Style.margin).toBe("0px");

    const c3P2Style = await evaluateComputedStyle(c3Paragraphs.nth(1));
    expect(parseFloat(c3P2Style.marginTop)).toBeCloseTo(paragraphMargin, 1);
    expect(c3P2Style.marginBottom).toBe("0px");

    // Container 5 (table) has no margins, and neither does its table
    const c5Style = await evaluateComputedStyle(containers.nth(4));
    expect(c5Style.marginTop).toBe("0px");
    expect(c5Style.marginBottom).toBe("0px");

    const c5TableStyle = await evaluateComputedStyle(containers.nth(4).locator("table"));
    expect(c5TableStyle.marginTop).toBe("0px");

    // Container 6 has no margins, and neither does its inner paragraph
    const c6Style = await evaluateComputedStyle(containers.nth(5));
    expect(c6Style.marginTop).toBe("0px");
    expect(c6Style.marginBottom).toBe("0px");

    const c6PStyle = await evaluateComputedStyle(containers.nth(5).locator("p"));
    expect(c6PStyle.margin).toBe("0px");
});
