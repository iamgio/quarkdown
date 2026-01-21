import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies global alignment correctly",
    ["plain", "paged", "slides"],
    async (page, docType) => {
        const heading = page.locator("h1").first();
        const paragraph = page.locator("p").first();
        const listItem = page.locator("li").first();

        const alignment = "end";
        await expect(heading).toHaveCSS("text-align", alignment);
        await expect(paragraph).toHaveCSS("text-align", alignment);
        await expect(paragraph).toHaveCSS("text-align-last", alignment);
        await expect(listItem).toHaveCSS("text-align", docType === "slides" ? "start" : "justify");
        await expect(listItem).toHaveCSS("text-align-last", "start");
    }
);
