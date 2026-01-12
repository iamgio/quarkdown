import {evaluateComputedStyle, getComputedSizeProperty} from "../__util/css";
import {suite} from "../quarkdown";

const {test, expect} = suite(__dirname);

test("renders horizontal and vertical rules", async (page) => {
    const blockMargin = await getComputedSizeProperty(page, "var(--qd-block-margin)");

    // Regular hr is horizontal (width > height)
    const regularHr = page.locator("hr").first();
    await expect(regularHr).toBeAttached();
    const regularBox = await regularHr.boundingBox();
    const regularStyle = await evaluateComputedStyle(regularHr);
    expect(regularBox).not.toBeNull();
    expect(regularBox!.width).toBeGreaterThan(regularBox!.height);
    expect(parseFloat(regularStyle.marginTop)).toBeCloseTo(blockMargin, 1);
    expect(regularStyle.marginBottom).toBe(regularStyle.marginTop);
    expect(regularStyle.marginLeft).not.toBe(regularStyle.marginTop);
    expect(regularStyle.marginRight).not.toBe(regularStyle.marginTop);

    // Hr in row is vertical (height > width, min 10px height)
    const rowHr = page.locator(".stack-row > hr");
    await expect(rowHr).toBeAttached();
    const rowBox = await rowHr.boundingBox();
    const rowStyle = await evaluateComputedStyle(rowHr);
    expect(rowBox).not.toBeNull();
    expect(rowBox!.height).toBeGreaterThan(rowBox!.width);
    expect(rowBox!.height).toBeGreaterThanOrEqual(10);
    expect(parseFloat(rowStyle.marginLeft)).toBeCloseTo(blockMargin, 1);
    expect(rowStyle.marginRight).toBe(rowStyle.marginLeft);
    expect(rowStyle.marginTop).not.toBe(rowStyle.marginLeft);
    expect(rowStyle.marginBottom).not.toBe(rowStyle.marginLeft);
});
