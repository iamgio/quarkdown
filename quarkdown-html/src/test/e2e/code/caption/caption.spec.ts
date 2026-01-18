import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("displays caption", async (page) => {
    await expect(page.locator("pre code.hljs")).toBeVisible();
    await expect(page.locator("figcaption")).toHaveText("A wrapper class");
});
