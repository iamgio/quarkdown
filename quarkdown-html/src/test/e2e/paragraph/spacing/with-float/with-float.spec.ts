import {evaluateComputedStyle, getComputedSizeProperty} from "../../../__util/css";
import {suite} from "../../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies correct spacing with floating figure", async (page) => {
    const paragraphMargin = await getComputedSizeProperty(page, "var(--qd-paragraph-vertical-margin)");
    const paragraphs = page.locator("p");
    const floats = page.locator(".float");
    await expect(paragraphs).toHaveCount(3);
    await expect(floats).toHaveCount(2);

    // First paragraph has no margins
    const p1Style = await evaluateComputedStyle(paragraphs.nth(0));
    expect(p1Style.margin).toBe("0px");

    // Second and third paragraphs have only margin-top
    for (const i of [0, 1]) {
        const pStyle = await evaluateComputedStyle(paragraphs.nth(i + 1));
        expect(parseFloat(pStyle.marginTop)).toBeCloseTo(paragraphMargin, 1);
        expect(pStyle.marginBottom).toBe("0px");
        expect(pStyle.marginLeft).toBe("0px");
        expect(pStyle.marginRight).toBe("0px");

        const float = floats.nth(i);
        await expect(float).toBeAttached();
        const floatStyle = await evaluateComputedStyle(float);
        expect(floatStyle.float).toBe(i == 0 ? "inline-start" : "inline-end");
        expect(parseFloat(floatStyle.marginTop)).toBeCloseTo(paragraphMargin, 1);
        expect(floatStyle.marginBottom).toBe("0px");

        if (i == 0) {
            expect(floatStyle.marginLeft).toBe("0px");
            expect(floatStyle.marginRight).not.toBe("0px");
        } else {
            expect(floatStyle.marginLeft).not.toBe("0px");
            expect(floatStyle.marginRight).toBe("0px");
        }

        const figure = float.locator("figure");
        await expect(figure).toBeAttached();
        const figureStyle = await evaluateComputedStyle(figure);
        expect(figureStyle.margin).toBe("0px");

        // Floating element has same Y as its paragraph
        const pBox = await paragraphs.nth(i + 1).boundingBox();
        const floatBox = await float.boundingBox();
        expect(pBox).not.toBeNull();
        expect(floatBox).not.toBeNull();
        expect(floatBox!.y).toBeCloseTo(pBox!.y, 0);
    }
});
