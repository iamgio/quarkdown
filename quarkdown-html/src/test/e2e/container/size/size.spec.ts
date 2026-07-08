import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies size styles to containers with increasing font sizes", async (page) => {
    const sizes = ["tiny", "small", "normal", "medium", "large", "larger", "huge"];
    let prevSize = 0;
    for (const size of sizes) {
        const locator = page.locator(`.container[style*="--qd-size-${size},"]`);
        await expect(locator).toHaveAttribute("style", new RegExp(`font-size: var\\(--qd-size-${size}, 1em\\)`));
        const fontSize = await locator.evaluate((e) => parseFloat(getComputedStyle(e).fontSize));
        expect(fontSize).toBeGreaterThan(prevSize);
        prevSize = fontSize;
    }
});
