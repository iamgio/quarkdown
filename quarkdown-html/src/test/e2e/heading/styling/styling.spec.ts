import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies multiple inline styles to a heading", async (page) => {
    const heading = page.locator("h2").first();

    const red = await getComputedColor(page, "#ff0000");
    const green = await getComputedColor(page, "#00ff00");

    await expect(heading).toHaveCSS("color", red);
    await expect(heading).toHaveCSS("background-color", green);
    await expect(heading).toHaveCSS("font-weight", "700");
    await expect(heading).toHaveCSS("font-style", "italic");
    await expect(heading).toHaveCSS("font-variant-caps", "small-caps");
    await expect(heading).toHaveCSS("text-decoration-line", "underline");
});
