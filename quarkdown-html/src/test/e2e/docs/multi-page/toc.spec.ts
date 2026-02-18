import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("page with content has visible heading and non-bold links", async (page) => {
    const heading = page.locator("h3#table-of-contents");
    await expect(heading).toBeAttached();
    await expect(heading).toBeVisible();

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

test("page without content has hidden heading and empty list", async (page) => {
    const heading = page.locator("h3#table-of-contents");
    await expect(heading).toBeAttached();
    await expect(heading).toHaveCSS("visibility", "hidden");

    const nav = page.locator('nav[data-role="table-of-contents"]');
    const list = nav.locator("> ol");
    await expect(list).toBeAttached();
    await expect(list).toBeEmpty();
}, {subpath: "page-1"});
