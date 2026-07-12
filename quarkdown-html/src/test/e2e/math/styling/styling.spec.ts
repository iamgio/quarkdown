import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies inline styles to block and inline math", async (page) => {
    const blockFormula = page.locator("formula[data-block]").first();
    const inlineFormula = page.locator("p formula").first();

    const red = await getComputedColor(page, "red");
    const black = await getComputedColor(page, "black");

    for (const formula of [blockFormula, inlineFormula]) {
        await expect(formula).toHaveCSS("color", red);
        await expect(formula).toHaveCSS("background-color", black);
        await expect(formula).toHaveCSS("padding", "10px");
    }
});
