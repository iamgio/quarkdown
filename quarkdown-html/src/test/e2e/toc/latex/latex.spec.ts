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
