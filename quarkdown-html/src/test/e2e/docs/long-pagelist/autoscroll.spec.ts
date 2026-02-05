import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("page list auto-scrolls to show current page", async (page) => {
    const aside = page.locator(".quarkdown-docs > .content-wrapper > aside:first-child");
    const currentPageLink = page.locator('nav[data-role="page-list"] [aria-current]');

    await expect(currentPageLink).toBeVisible();

    // Verify the aside has scrolled (scrollTop > 0 since current page is at the end of the list)
    const scrollTop = await aside.evaluate((el) => el.scrollTop);
    expect(scrollTop).toBeGreaterThan(0);

    // Verify the current page link is visible within the aside's viewport
    const asideBox = await aside.boundingBox();
    const currentLinkBox = await currentPageLink.boundingBox();

    expect(asideBox).not.toBeNull();
    expect(currentLinkBox).not.toBeNull();

    // The link should be within the visible portion of the aside
    const linkTop = currentLinkBox!.y;
    const linkBottom = linkTop + currentLinkBox!.height;
    const asideTop = asideBox!.y;
    const asideBottom = asideTop + asideBox!.height;

    expect(linkTop).toBeGreaterThanOrEqual(asideTop);
    expect(linkBottom).toBeLessThanOrEqual(asideBottom);
}, {subpath: "page"});
