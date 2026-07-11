import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies multiple inline styles to a paragraph", async (page) => {
    const paragraph = page.locator("p").first();

    const red = await getComputedColor(page, "red");
    const black = await getComputedColor(page, "black");

    await expect(paragraph).toHaveCSS("color", red);
    await expect(paragraph).toHaveCSS("background-color", black);
    await expect(paragraph).toHaveCSS("padding", "10px");
    await expect(paragraph).toHaveCSS("text-transform", "uppercase");
    await expect(paragraph).toHaveCSS("font-variant-caps", "small-caps");
});
