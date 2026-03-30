import {SM_WIDTH} from "../../__util/breakpoints";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

type Alignment = { global: string; local: string; listItem: string };

function expectedAlignment(docType: string): Alignment {
    if (docType === "slides" || docType === "slides-print") {
        return {global: "center", local: "center", listItem: "start"};
    }
    if (docType === "plain" || docType === "paged") {
        return {global: "start", local: "justify", listItem: "justify"};
    }
    return {global: "start", local: "start", listItem: "start"};
}

async function assertAlignment(page: import("@playwright/test").Page, alignment: Alignment) {
    const heading = page.locator("h1").first();
    const paragraph = page.locator("p").first();
    const listItem = page.locator("li").first();
    const inlineMath = page.locator("p > formula").first();
    const blockMath = page.locator("formula[data-block]").first();

    await expect(heading).toHaveCSS("text-align", alignment.global);
    await expect(paragraph).toHaveCSS("text-align", alignment.local);
    await expect(paragraph).toHaveCSS("text-align-last", alignment.global);
    await expect(listItem).toHaveCSS("text-align", alignment.listItem);
    await expect(listItem).toHaveCSS("text-align-last", "start");
    await expect(inlineMath).toHaveCSS("text-align", "start");
    await expect(inlineMath).toHaveCSS("text-align-last", "auto");
    await expect(blockMath).toHaveCSS("text-align", "start");
    await expect(blockMath).toHaveCSS("text-align-last", "auto");
}

testMatrix(
    "applies correct text alignment per doctype",
    ["plain", "paged", "slides", "slides-print", "docs"],
    async (page, docType) => {
        await assertAlignment(page, expectedAlignment(docType));
    }
);

testMatrix(
    "plain uses start alignment on small screens",
    ["plain"],
    async (page) => {
        await page.setViewportSize({width: SM_WIDTH, height: 800});
        await assertAlignment(page, {global: "start", local: "start", listItem: "start"});
    }
);
