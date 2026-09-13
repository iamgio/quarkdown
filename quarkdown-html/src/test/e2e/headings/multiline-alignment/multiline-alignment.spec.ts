import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("multiline headings are aligned to the left", async (page) => {
    const heading = page.locator("h1").first();
    await expect(heading).toHaveCSS("display", "flex");
});
