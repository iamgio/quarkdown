import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("renders inline whitespace as an inline element inside the paragraph", async (page) => {
    await expect(page.locator("p")).toHaveCount(1);

    const whitespace = page.locator("p > span.whitespace");
    await expect(whitespace).toHaveCount(1);
    await expect(whitespace).toHaveCSS("display", "inline-block");
    await expect(whitespace).toHaveText("\u00a0");
    await expect(whitespace).toHaveCSS("margin-top", "0px");
});
