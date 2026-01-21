import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies CSS property overrides", async (page) => {
    const link = page.locator("a");

    await expect(link).toBeAttached();
    await expect(link).toHaveCSS("color", "rgb(255, 0, 0)");
});
