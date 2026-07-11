import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies inline styles to a paragraph and its inline content", async (page) => {
    const paragraph = page.locator("p").first();

    const red = await getComputedColor(page, "red");
    const black = await getComputedColor(page, "black");

    await expect(paragraph).toHaveCSS("color", red);
    await expect(paragraph).toHaveCSS("background-color", black);
    await expect(paragraph).toHaveCSS("padding", "10px");
    await expect(paragraph).toHaveCSS("text-transform", "uppercase");
    await expect(paragraph).toHaveCSS("font-variant-caps", "small-caps");

    // Inline content (codespan, emph, strong) inherits the paragraph color.
    await expect(paragraph.locator("code")).toHaveCSS("color", red);
    await expect(paragraph.locator("em")).toHaveCSS("color", red);
    await expect(paragraph.locator("strong")).toHaveCSS("color", red);
});
