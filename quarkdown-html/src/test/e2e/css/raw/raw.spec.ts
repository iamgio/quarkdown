import {DocumentType, suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);
const documentTypes: DocumentType[] = ["plain", "paged", "slides", "docs"];

testMatrix("moves raw CSS out of document content and applies it", documentTypes, async (page, documentType) => {
    const link = page.getByRole("link", {name: "Link"});

    if (documentType !== "paged") {
        await expect(page.locator("head > style[data-hidden]")).toHaveCount(1);
    }
    await expect(page.locator("body style[data-hidden]")).toHaveCount(0);
    await expect(link).toBeAttached();
    await expect(link).toHaveCSS("color", "rgb(255, 0, 0)");
});

testMatrix(
    "does not create a blank page before the first heading",
    ["paged"],
    async (page) => {
        const pages = page.locator(".pagedjs_page");

        await expect(pages).toHaveCount(1);
        await expect(pages.first().getByRole("heading", {name: "Heading"})).toBeVisible();
        await expect(pages.first().getByRole("link", {name: "Link"})).toHaveCSS("color", "rgb(255, 0, 0)");
    }
);
