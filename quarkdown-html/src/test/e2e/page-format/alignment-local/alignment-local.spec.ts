import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies local alignment correctly",
    ["plain", "paged", "slides"],
    async (page, docType) => {
        const heading = page.locator("h1").first();
        const paragraph = page.locator("p").first();
        const listItem = page.locator("li").first();

        const localAlignment = "justify";
        const globalAlignment = "start";
        await expect(heading).toHaveCSS("text-align", globalAlignment);
        await expect(paragraph).toHaveCSS("text-align", localAlignment);
        await expect(paragraph).toHaveCSS("text-align-last", globalAlignment);
        await expect(listItem).toHaveCSS("text-align", localAlignment);
        await expect(listItem).toHaveCSS("text-align-last", globalAlignment);
    }
);
