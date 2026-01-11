import {suite} from "../quarkdown";

const {test, expect} = suite(__dirname);

test("renders horizontal and vertical rules", async (page) => {
    // Regular hr is horizontal (width > height)
    const regularHr = page.locator("hr").first();
    const regularBox = await regularHr.boundingBox();
    expect(regularBox).not.toBeNull();
    expect(regularBox!.width).toBeGreaterThan(regularBox!.height);

    // Hr in row is vertical (height > width, min 10px height)
    const rowHr = page.locator(".stack-row > hr");
    const rowBox = await rowHr.boundingBox();
    expect(rowBox).not.toBeNull();
    expect(rowBox!.height).toBeGreaterThan(rowBox!.width);
    expect(rowBox!.height).toBeGreaterThanOrEqual(10);
});
