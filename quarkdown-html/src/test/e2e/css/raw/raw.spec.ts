import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies raw CSS styles", async (page) => {
    const link = page.locator("a");

    await expect(link).toBeAttached();
    await expect(link).toHaveCSS("color", "rgb(255, 0, 0)");
});
