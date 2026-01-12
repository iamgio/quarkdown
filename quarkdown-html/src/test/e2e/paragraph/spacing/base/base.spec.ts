import {evaluateComputedStyle, getComputedSizeProperty} from "../../../__util/css";
import {suite} from "../../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies correct paragraph spacing", async (page) => {
    const paragraphMargin = await getComputedSizeProperty(page, "var(--qd-paragraph-vertical-margin)");
    const paragraphs = page.locator("p");
    await expect(paragraphs).toHaveCount(3);

    // First paragraph has no margins,
    // Second and third paragraphs have only margin-top
    for (const i of [0, 1, 2]) {
        const style = await evaluateComputedStyle(paragraphs.nth(i));
        if (i == 0) {
            expect(style.marginTop).toBe("0px");
        } else {
            expect(parseFloat(style.marginTop)).toBeCloseTo(paragraphMargin, 1);
        }
        expect(style.marginBottom).toBe("0px");
        expect(style.marginLeft).toBe("0px");
        expect(style.marginRight).toBe("0px");
    }
});
