import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies inline styles to a link", async (page) => {
    const link = page.locator("p a").first();

    const red = await getComputedColor(page, "red");
    const black = await getComputedColor(page, "black");

    await expect(link).toHaveCSS("color", red);
    await expect(link).toHaveCSS("background-color", black);
    await expect(link).toHaveCSS("padding", "4px");
});
