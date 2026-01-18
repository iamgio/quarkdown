import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies font variant", async (page) => {
    await expect(page.locator("text=small caps")).toHaveCSS("font-variant", /small-caps/);
});
