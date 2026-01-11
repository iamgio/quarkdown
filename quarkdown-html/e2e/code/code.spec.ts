import {suite} from "../quarkdown";

const {test, expect} = suite(__dirname);

test("highlights code blocks", async (page) => {
    await expect(page.locator("pre code.hljs")).toBeVisible();
});
