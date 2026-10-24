import {suite} from "../../quarkdown";
import {overflowingTexts} from "../../__util/paged";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "breaks pages between words, never inside a hyphenated one",
    ["paged"],
    async (page) => {
        expect(await page.locator(".pagedjs_page").count()).toBeGreaterThan(2);
        // paged.js marks the element it cut mid-word.
        await expect(page.locator(".pagedjs_hyphen")).toHaveCount(0);
        expect(await overflowingTexts(page)).toEqual([]);
    },
);
