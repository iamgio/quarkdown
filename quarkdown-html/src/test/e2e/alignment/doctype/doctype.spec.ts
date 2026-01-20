import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies correct text alignment per doctype",
    ["plain", "paged", "slides", "slides-print", "docs"],
    async (page, docType) => {
        const heading = page.locator("h1").first();
        const paragraph = page.locator("p").first();
        const listItem = page.locator("li").first();
        const inlineMath = page.locator("p > formula").first();
        const blockMath = page.locator("formula[data-block]").first();

        let alignmentGlobal = "start";
        let alignmentLocal = "start";
        let alignmentListItem = "start";

        if (docType === "slides" || docType === "slides-print") {
            alignmentGlobal = "center";
            alignmentLocal = "center";
        } else if (docType === "plain" || docType === "paged") {
            alignmentLocal = "justify";
            alignmentListItem = "justify";
        }

        await expect(heading).toHaveCSS("text-align", alignmentGlobal);
        await expect(paragraph).toHaveCSS("text-align", alignmentLocal);
        await expect(paragraph).toHaveCSS("text-align-last", alignmentGlobal);
        await expect(listItem).toHaveCSS("text-align", alignmentListItem);
        await expect(listItem).toHaveCSS("text-align-last", "start");
        await expect(inlineMath).toHaveCSS("text-align", "start");
        await expect(inlineMath).toHaveCSS("text-align-last", "auto");
        await expect(blockMath).toHaveCSS("text-align", "start");
        await expect(blockMath).toHaveCSS("text-align-last", "auto");
    }
);
