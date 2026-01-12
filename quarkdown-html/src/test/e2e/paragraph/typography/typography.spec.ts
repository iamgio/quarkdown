import {evaluateComputedStyle} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies correct default paragraph typography", async (page) => {
    const paragraph = page.locator("p").first();
    await expect(paragraph).toBeAttached();

    const style = await evaluateComputedStyle(paragraph);

    // --qd-line-height is unitless (e.g., 1.5), so compare the ratio
    const lineHeightRatio = parseFloat(style.lineHeight) / parseFloat(style.fontSize);
    const expectedRatio = await page.evaluate(() =>
        parseFloat(getComputedStyle(document.documentElement).getPropertyValue("--qd-line-height"))
    );
    expect(lineHeightRatio).toBeCloseTo(expectedRatio, 1);

    expect(style.letterSpacing).toBe("normal");
    expect(style.textIndent).toBe("0px");
});
