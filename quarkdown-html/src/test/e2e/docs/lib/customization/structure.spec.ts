import {suite} from "../../../quarkdown";

const {test, expect} = suite(__dirname);

test("page list is at righttop and toc is at lefttop", async (page) => {
    // Body should have quarkdown-docs class and be visible
    const body = page.locator("body.quarkdown-docs");
    await expect(body).toBeVisible();

    // Table of contents should be in the LEFT aside (customized from default)
    const leftAside = page.locator(".quarkdown-docs > .content-wrapper > aside:first-child");
    const toc = leftAside.locator('nav[data-role="table-of-contents"]');
    await expect(toc).toBeAttached();

    // Page list should be in the RIGHT aside (customized from default)
    const rightAside = page.locator(".quarkdown-docs > .content-wrapper > aside:last-child");
    const pageList = rightAside.locator('nav[data-role="page-list"]');
    await expect(pageList).toBeAttached();
});
