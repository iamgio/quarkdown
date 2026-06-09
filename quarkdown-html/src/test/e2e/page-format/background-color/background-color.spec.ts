import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies page background color correctly",
    ["plain", "docs", "paged", "slides", "slides-print"],
    async (page, docType) => {
        const background = "rgb(255, 0, 0)";

        // The background is painted on the page itself in paged documents,
        // and on the body for every other document type (see _viewport.scss).
        const target =
            docType === "paged" ? page.locator(".pagedjs_page").first() : page.locator("body");

        await expect(target).toHaveCSS("background-color", background);
    }
);
