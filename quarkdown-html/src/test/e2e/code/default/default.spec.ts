import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("highlights code and shows line numbers", async (page) => {
    const code = page.locator("pre code.hljs");
    await expect(code).toBeVisible();
    await expect(page.locator(".hljs-ln-numbers")).toHaveCount(11);
    // atom-one-dark (darko theme) background color.
    await expect(code).toHaveCSS("background-color", "rgb(40, 44, 52)");
});
