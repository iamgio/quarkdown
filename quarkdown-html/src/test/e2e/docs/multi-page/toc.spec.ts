import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("latex table of contents has no bold links", async (page) => {
    const nav = page.locator('nav[data-role="table-of-contents"]');
    await expect(nav).toBeAttached();

    const links = nav.locator("a");
    const count = await links.count();
    expect(count).toBeGreaterThan(0);

    // No links should be bold
    for (const link of await links.all()) {
        await expect(link).toHaveCSS("font-weight", "400");
    }
}, {subpath: "page-2"});
