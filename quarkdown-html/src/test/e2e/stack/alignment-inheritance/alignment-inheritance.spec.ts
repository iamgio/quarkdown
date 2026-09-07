import {evaluateComputedStyle} from "../../__util/css";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix("row without explicit alignment inherits the global alignment", ["plain", "slides"], async (page, docType) => {
    const row = page.locator(".stack.stack-row").first();
    const globalAlignment = (await evaluateComputedStyle(page.locator("body.quarkdown"))).textAlign;
    await expect(row).toHaveCSS("justify-content", globalAlignment);

    if (docType === "plain") {
        // Start-aligned document: the row's content starts at the leading edge.
        const stackBox = (await row.boundingBox())!;
        const childBox = (await row.locator("p").boundingBox())!;
        expect(childBox.x).toBeCloseTo(stackBox.x, 0);
    }
});

testMatrix("explicit alignment overrides the inherited one", ["plain", "slides"], async (page) => {
    const row = page.locator(".stack.stack-row").nth(1);
    await expect(row).toHaveCSS("justify-content", "flex-end");

    const stackBox = (await row.boundingBox())!;
    const childBox = (await row.locator("p").boundingBox())!;
    expect(childBox.x + childBox.width).toBeCloseTo(stackBox.x + stackBox.width, 0);
});

testMatrix("cross axis defaults to center", ["plain", "slides"], async (page) => {
    for (const stack of await page.locator(".stack").all()) {
        await expect(stack).toHaveCSS("align-items", "center");
    }
});
