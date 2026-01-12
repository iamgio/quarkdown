import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies font style", async (page) => {
    await expect(page.locator("text=normal")).toHaveCSS("font-style", "normal");
    await expect(page.locator("text=italic")).toHaveCSS("font-style", "italic");
});
