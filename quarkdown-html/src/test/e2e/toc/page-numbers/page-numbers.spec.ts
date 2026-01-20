import {suite} from "../../quarkdown";
import {assertTocStructure} from "../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "renders table of contents with page numbers for paged doctypes",
    ["plain", "paged", "slides", "slides-print", "docs"],
    async (page, docType) => {
        const count = 4;
        const {nav, items} = await assertTocStructure(page, count);

        const pageNumbers = nav.locator(".toc-page-number");

        if (docType === "paged" || docType === "slides" || docType === "slides-print") {
            await expect(pageNumbers).toHaveCount(count);

            // Check page number values
            for (let i = 0; i < count; i++) {
                const number = i + (i >= 2 ? 18 : 2); // First page is ToC, last two page are reset to 20
                await expect(pageNumbers.nth(i)).toHaveText(`${number}`);
            }

            // Check positioning: page numbers should be in the last fourth of the item
            const itemBox = await items.first().boundingBox();
            const pageNumBox = await pageNumbers.first().boundingBox();
            expect(itemBox).not.toBeNull();
            expect(pageNumBox).not.toBeNull();

            const itemRight = itemBox!.x + itemBox!.width;
            const lastFourthStart = itemBox!.x + itemBox!.width * 0.75;
            expect(pageNumBox!.x).toBeGreaterThanOrEqual(lastFourthStart);
            expect(pageNumBox!.x + pageNumBox!.width).toBeLessThanOrEqual(itemRight + 1);
        } else {
            // Plain and docs don't have page numbers
            await expect(pageNumbers).toHaveCount(0);
        }
    }
);
