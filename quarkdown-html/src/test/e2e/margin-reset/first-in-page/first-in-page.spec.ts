import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "first heading on each page has zero margin-top",
    ["paged"],
    async (page) => {
        const pages = page.locator(".pagedjs_page");
        await expect(pages).toHaveCount(3);

        // Page 1: # 1 (first, margin-top = 0), # 2 (not first), ## 3 (not first)
        const page1Headings = pages.nth(0).locator(":is(h1, h2)");
        await expect(page1Headings).toHaveCount(3);
        await expect(page1Headings.nth(0)).toHaveCSS("margin-top", "0px");
        await expect(page1Headings.nth(1)).not.toHaveCSS("margin-top", "0px");
        await expect(page1Headings.nth(2)).not.toHaveCSS("margin-top", "0px");

        // Page 2: # 4 (first, margin-top = 0)
        const page2Headings = pages.nth(1).locator(":is(h1, h2)");
        await expect(page2Headings).toHaveCount(1);
        await expect(page2Headings.nth(0)).toHaveCSS("margin-top", "0px");

        // Page 3: ## 5 (first, margin-top = 0)
        const page3Headings = pages.nth(2).locator(":is(h1, h2)");
        await expect(page3Headings).toHaveCount(1);
        await expect(page3Headings.nth(0)).toHaveCSS("margin-top", "0px");
    },
);
