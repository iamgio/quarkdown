import {evaluateComputedStyle, getComputedSizeProperty} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies custom paragraph styling", async (page) => {
    const em = await getComputedSizeProperty(page, "1em");
    const paragraphs = page.locator("p");
    await expect(paragraphs).toHaveCount(2);

    const p1Style = await evaluateComputedStyle(paragraphs.nth(0));
    const p2Style = await evaluateComputedStyle(paragraphs.nth(1));

    expect(parseFloat(p1Style.lineHeight)).toBeCloseTo(3 * em, 1);
    expect(parseFloat(p1Style.letterSpacing)).toBeCloseTo(2 * em, 1);
    expect(p1Style.textIndent).toBe("0px"); // first paragraph has no indent by design
    expect(parseFloat(p2Style.textIndent)).toBeCloseTo(2 * em, 1);
    expect(p1Style.marginTop).toBe("0px");
    expect(p2Style.marginTop).toBe("0px");
});
