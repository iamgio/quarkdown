import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies font weight", async (page) => {
    await expect(page.locator("text=normal")).toHaveCSS("font-weight", "400");
    await expect(page.locator("text=bold")).toHaveCSS("font-weight", "700");
});
