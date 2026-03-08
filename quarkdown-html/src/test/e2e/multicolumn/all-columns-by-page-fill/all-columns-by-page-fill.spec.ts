import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "all pages have both columns filled",
    ["paged"],
    async (page) => {
        const pages = page.locator(".pagedjs_page");
        const pageCount = await pages.count();
        expect(pageCount).toBeGreaterThan(1);

        // The last page may not fill both columns, so only check pages before it.
        for (let i = 0; i < pageCount - 1; i++) {
            const paragraphs = pages.nth(i).locator("p");
            const count = await paragraphs.count();
            expect(count).toBeGreaterThan(0);

            // Collect distinct column x-positions (with a tolerance for rounding).
            const columnXs: number[] = [];
            for (let j = 0; j < count; j++) {
                const box = await paragraphs.nth(j).boundingBox();
                if (box && !columnXs.some((x) => Math.abs(x - box.x) < 5)) {
                    columnXs.push(box.x);
                }
            }

            expect(columnXs, `page ${i + 1} should have exactly 2 columns`).toHaveLength(2);
        }
    },
);
