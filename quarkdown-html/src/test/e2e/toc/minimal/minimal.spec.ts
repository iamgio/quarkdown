import {suite} from "../../quarkdown";
import {assertTocLinks, assertTocNumbering, assertTocStructure} from "../index";

const {test, expect} = suite(__dirname);

test("renders table of contents with correct structure", async (page) => {
    const {nav} = await assertTocStructure(page);

    // Root list has no border
    const rootOl = nav.locator("> ol");
    await expect(rootOl).toHaveCSS("border-left-style", "none");

    // Nested lists have border-left
    const nestedOl = nav.locator("ol ol").first();
    await expect(nestedOl).toHaveCSS("border-left-style", "solid");
    await expect(nestedOl).toHaveCSS("border-left-width", "2px");
});

test("displays correct numbering in li::before", async (page) => {
    await assertTocNumbering(page, "minimal");
});

test("location markers have reduced opacity", async (page) => {
    const nav = page.locator("nav");
    const item = nav.locator("li").first();

    const opacity = await item.evaluate((el) => {
        return getComputedStyle(el, "::before").opacity;
    });
    expect(parseFloat(opacity)).toBeLessThan(1);
});

test("links to correct heading anchors", async (page) => {
    await assertTocLinks(page);
});
