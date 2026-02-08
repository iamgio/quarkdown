import {suite} from "../../../quarkdown";

const {test, expect} = suite(__dirname);

const pages = [
    {subpath: undefined, title: "My docs", heading: "Main title"},
    {subpath: "page-1", title: "Page 1", heading: "Page 1 title"},
    {subpath: "page-2", title: "Page 2", heading: "Page 2 title"},
    {subpath: "nested-page", title: "Nested Page", heading: "Nested Page title"},
];

for (const {subpath, title, heading} of pages) {
    const pageName = subpath ?? "root";

    test(`${pageName}: docs body is visible with correct margins`, async (page) => {
        // Body should have quarkdown-docs class and be visible
        const body = page.locator("body.quarkdown-docs");
        await expect(body).toBeVisible();

        // Dark color scheme from darko theme
        await expect(page.locator("html")).toHaveCSS("color-scheme", "dark");

        // Page list should be in the left aside
        const leftAside = page.locator(".quarkdown-docs > .content-wrapper > aside:first-child");
        const pageList = leftAside.locator('nav[data-role="page-list"]');
        await expect(pageList).toBeAttached();

        // Table of contents should be in the right aside
        const rightAside = page.locator(".quarkdown-docs > .content-wrapper > aside:last-child");
        const toc = rightAside.locator('nav[data-role="table-of-contents"]');
        await expect(toc).toBeAttached();
    }, {subpath});

    test(`${pageName}: has correct title and heading`, async (page) => {
        // Check document title
        await expect(page).toHaveTitle(title);

        // Check h1 matches docname and is the only h1
        const h1 = page.locator(".quarkdown-docs > .content-wrapper > main h1");
        await expect(h1).toHaveCount(1);
        await expect(h1).toHaveText(title);

        // Check h2 heading in main content
        const h2 = page.locator(".quarkdown-docs > .content-wrapper > main h2");
        await expect(h2.first()).toHaveText(heading);
    }, {subpath});
}
