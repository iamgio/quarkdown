import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

/**
 * Collects distinct column x-positions from a set of locators, with a tolerance for rounding.
 */
async function distinctColumnCount(locator: import("@playwright/test").Locator): Promise<number> {
    const xs: number[] = [];
    const count = await locator.count();
    for (let i = 0; i < count; i++) {
        const box = await locator.nth(i).boundingBox();
        if (box && !xs.some((x) => Math.abs(x - box.x) < 5)) {
            xs.push(box.x);
        }
    }
    return xs.length;
}

testMatrix(
    "full-span elements span across all columns",
    ["plain", "paged", "slides", "slides-print", "docs"],
    async (page) => {
        const paragraphs = page.locator("p");
        const h2 = page.locator("h2");
        const fullSpan = page.locator(".full-column-span");

        // Paragraphs are laid out in two columns.
        expect(await distinctColumnCount(paragraphs)).toBe(2);

        // h2 has column-span: all and spans the full parent width.
        await expect(h2).toHaveCSS("column-span", "all");
        const h2Box = (await h2.boundingBox())!;
        const parentBox = (await h2.locator("..").boundingBox())!;
        expect(h2Box.width).toBeGreaterThan(parentBox.width * 0.9);

        // .full-column-span has the same span properties as the h2.
        await expect(fullSpan).toHaveCSS("column-span", "all");
        const fullSpanBox = (await fullSpan.boundingBox())!;
        expect(fullSpanBox.width).toBeGreaterThan(parentBox.width * 0.9);
    },
);
