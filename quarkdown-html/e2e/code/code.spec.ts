import {expect, test, testDocument} from "../quarkdown";

test("highlights code blocks", async ({page}) => {
    await testDocument(__dirname, page, async (page) => {
        await expect(page.locator("pre code.hljs")).toBeVisible();
    });
});
