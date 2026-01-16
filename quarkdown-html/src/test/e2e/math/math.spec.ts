import {getComputedColor} from "../__util/css";
import {suite} from "../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "renders block and inline math correctly",
    ["plain", "paged", "slides", "docs"],
    async (page) => {
        const blockFormula = page.locator("formula[data-block] > .katex-display");
        const inlineFormula = page.locator("p formula > .katex");

        await expect(blockFormula).toBeAttached();
        await expect(inlineFormula).toBeAttached();

        const mainColor = await getComputedColor(page, "var(--qd-main-color)");
        await expect(blockFormula).toHaveCSS("color", mainColor);
        await expect(inlineFormula).toHaveCSS("color", mainColor);

        const blockBox = await blockFormula.boundingBox();
        const inlineBox = await inlineFormula.boundingBox();

        expect(blockBox).not.toBeNull();
        expect(inlineBox).not.toBeNull();

        expect(blockBox!.width).toBeGreaterThan(inlineBox!.width);
        expect(blockBox!.y).toBeLessThan(inlineBox!.y);

        const blockBaseBox = await blockFormula.locator(".base").first().boundingBox();
        const inlineBaseBox = await inlineFormula.locator(".base").first().boundingBox();

        expect(blockBaseBox!.x).toBeGreaterThan(inlineBaseBox!.x);
    }
);
