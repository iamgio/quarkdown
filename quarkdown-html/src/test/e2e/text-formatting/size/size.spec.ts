import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies size classes with increasing font sizes", async (page) => {
    const sizes = ["tiny", "small", "normal", "medium", "large", "larger", "huge"];
    let prevSize = 0;
    for (const size of sizes) {
        const fontSize = await page.locator(`.size-${size}`).evaluate((e) => parseFloat(getComputedStyle(e).fontSize));
        expect(fontSize).toBeGreaterThan(prevSize);
        prevSize = fontSize;
    }
});
