import {suite} from "../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "renders page breaks",
    ["paged", "slides", "slides-print"],
    async (page, docType) => {
        const expectedPageCount = 3;
        switch (docType) {
            case "paged":
                await expect(page.locator(".pagedjs_page")).toHaveCount(expectedPageCount);
                break;
            case "slides":
            case "slides-print":
                await expect(page.locator(".reveal .slides > *")).toHaveCount(expectedPageCount);
                await expect(page.locator(".reveal .slide-background")).toHaveCount(expectedPageCount);
                break;
        }
    }
);
