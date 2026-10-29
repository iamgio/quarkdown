import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

const BLACK = "rgb(0, 0, 0)";

testMatrix("uses a soft black text color on screen", ["plain", "slides"], async (page) => {
    const text = page.locator("p").first();
    await expect(text).not.toHaveCSS("color", BLACK);
});

testMatrix("uses pure black text in paged documents", ["paged"], async (page) => {
    await expect(page.locator("p").first()).toHaveCSS("color", BLACK);
    await expect(page.locator("h1").first()).toHaveCSS("color", BLACK);
});

testMatrix("uses pure black text in print media", ["plain"], async (page) => {
    await page.emulateMedia({media: "print"});
    await expect(page.locator("p").first()).toHaveCSS("color", BLACK);
    await expect(page.locator("h1").first()).toHaveCSS("color", BLACK);
});
