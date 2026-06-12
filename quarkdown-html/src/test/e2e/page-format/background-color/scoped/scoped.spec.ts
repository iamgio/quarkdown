import {suite} from "../../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

const RED = "rgb(255, 0, 0)";

testMatrix(
    "applies scoped page background color to the selected page range",
    ["paged"],
    async (page) => {
        // Source has 4 pages; red background is scoped to pages 1..2.
        const pages = page.locator(".pagedjs_page");
        await expect(pages).toHaveCount(4);

        await expect(pages.nth(0)).toHaveCSS("background-color", RED);
        await expect(pages.nth(1)).toHaveCSS("background-color", RED);
        await expect(pages.nth(2)).not.toHaveCSS("background-color", RED);
        await expect(pages.nth(3)).not.toHaveCSS("background-color", RED);
    }
);
