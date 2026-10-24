import {suite} from "../../quarkdown";
import {overflowingTexts} from "../../__util/paged";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "paginates text without word separators across pages",
    ["paged"],
    async (page) => {
        expect(await page.locator(".pagedjs_page").count()).toBeGreaterThan(2);
        expect(await overflowingTexts(page)).toEqual([]);

        // Every page keeps text, so no page break became a paragraph break leaving a page empty.
        const pageTextLengths = await page.locator(".pagedjs_page_content").evaluateAll(
            contents => contents.map(content => (content.textContent ?? "").trim().length),
        );
        pageTextLengths.slice(0, -1).forEach(length => expect(length).toBeGreaterThan(300));
    },
);
