import {suite} from "../../quarkdown";
import {assertTocLinks, assertTocNumbering, assertTocStructure} from "../index";

const {test, expect} = suite(__dirname);

test("renders table of contents with correct structure", async (page) => {
    const {nav, items} = await assertTocStructure(page);

    // No borders on list elements
    const ol = nav.locator("ol");
    await expect(ol.first()).toHaveCSS("border-style", "none");
    await expect(items.first()).toHaveCSS("border-style", "none");
});

test("displays correct numbering in li::before", async (page) => {
    await assertTocNumbering(page, "latex");
});

test("links to correct heading anchors", async (page) => {
    await assertTocLinks(page);
});

test("first-level links are bold, others are normal", async (page) => {
    const nav = page.locator("nav");
    const firstLevelLinks = nav.locator("> :is(ul, ol) > li > a");
    const nestedLinks = nav.locator(":is(ul, ol) :is(ul, ol) a");

    // First-level links are bold
    for (const link of await firstLevelLinks.all()) {
        await expect(link).toHaveCSS("font-weight", "700");
    }

    // Nested links are normal
    for (const link of await nestedLinks.all()) {
        await expect(link).toHaveCSS("font-weight", "400");
    }
});
