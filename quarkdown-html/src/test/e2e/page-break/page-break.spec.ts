import {DocumentType, suite} from "../quarkdown";

const {testMatrix, expect} = suite(__dirname);

const types: DocumentType[] = ["paged", "slides"];
const expectedPageCount = 3;

testMatrix("renders page breaks", types, async (page, docType) => {
    switch (docType) {
        case "paged":
            await expect(page.locator(".pagedjs_page")).toHaveCount(expectedPageCount);
            break;
        case "slides":
            await expect(page.locator(".reveal .slides > section")).toHaveCount(expectedPageCount);
            await expect(page.locator(".reveal .backgrounds > .slide-background")).toHaveCount(expectedPageCount);
            break;
    }
});
