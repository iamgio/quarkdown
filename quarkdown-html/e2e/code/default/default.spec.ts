import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("highlights code and shows line numbers", async (page) => {
    await expect(page.locator("pre code.hljs")).toBeVisible();
    await expect(page.locator(".hljs-ln-numbers")).toHaveCount(11);
});
