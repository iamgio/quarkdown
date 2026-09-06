import {evaluateComputedStyle, getComputedSizeProperty} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("renders block whitespace as a block element with block margin", async (page) => {
    const whitespace = page.locator("div.whitespace");
    await expect(whitespace).toHaveCount(1);
    await expect(whitespace).toHaveCSS("display", "block");
    await expect(whitespace).toHaveText("\u00a0");

    const blockMargin = await getComputedSizeProperty(page, "var(--qd-block-margin)");
    const style = await evaluateComputedStyle(whitespace);
    expect(parseFloat(style.marginTop)).toBeCloseTo(blockMargin, 1);
});
