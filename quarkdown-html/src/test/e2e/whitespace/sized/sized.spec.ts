import {evaluateComputedStyle, getComputedSizeProperty} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("sized block whitespace occupies the given size", async (page) => {
    const whitespace = page.locator("div.whitespace");
    await expect(whitespace).toHaveCount(1);

    const style = await evaluateComputedStyle(whitespace);
    expect(parseFloat(style.width)).toBeCloseTo(await getComputedSizeProperty(page, "3cm"), 1);
    expect(parseFloat(style.height)).toBeCloseTo(await getComputedSizeProperty(page, "2cm"), 1);
});

test("sized inline whitespace reserves horizontal space", async (page) => {
    const whitespace = page.locator("span.whitespace");
    await expect(whitespace).toHaveCount(1);

    const box = await whitespace.boundingBox();
    expect(box).not.toBeNull();
    expect(box!.width).toBeCloseTo(await getComputedSizeProperty(page, "2cm"), 1);
});
