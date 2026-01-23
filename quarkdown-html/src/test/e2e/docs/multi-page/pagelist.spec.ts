import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("current page link is bold, others are normal", async (page) => {
    const nav = page.locator('nav[data-role="page-list"]');
    await expect(nav).toBeAttached();

    const links = nav.locator("a");
    await expect(links).toHaveCount(3);

    // Page 2 link (current) should be bold
    const page2Link = links.filter({hasText: "Page 2"});
    await expect(page2Link).toHaveCSS("font-weight", "700");

    // Other links should be normal weight
    const page1Link = links.filter({hasText: "Page 1"});
    const page3Link = links.filter({hasText: "Page 3"});
    await expect(page1Link).toHaveCSS("font-weight", "400");
    await expect(page3Link).toHaveCSS("font-weight", "400");
}, {subpath: "page-2"});
