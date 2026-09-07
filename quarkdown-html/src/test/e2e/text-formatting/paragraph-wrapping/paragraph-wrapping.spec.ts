import {evaluateComputedStyle, getComputedSizeProperty} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("block text call is wrapped in a paragraph with paragraph spacing", async (page) => {
    const paragraphs = page.locator("p");
    await expect(paragraphs).toHaveCount(3);

    const wrapped = paragraphs.nth(1);
    await expect(wrapped.locator("span")).toHaveText("Styled text");

    const paragraphMargin = await getComputedSizeProperty(page, "var(--qd-paragraph-vertical-margin)");
    const style = await evaluateComputedStyle(wrapped);
    expect(parseFloat(style.marginTop)).toBeCloseTo(paragraphMargin, 1);
});
