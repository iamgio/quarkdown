import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "inherits alignment in containers",
    ["plain", "paged"],
    async (page) => {
        const paragraphs = page.locator("p");
        const first = paragraphs.nth(0);
        const second = paragraphs.nth(1);
        const third = paragraphs.nth(2);

        // First paragraph: regular, not inside a container.
        await expect(first).toHaveCSS("text-align", "justify");
        await expect(first).toHaveCSS("text-align-last", "start");

        // Second paragraph: direct child of .center,
        // hence [style*="text-align"] > p unsets alignment, inheriting center from parent.
        await expect(second).toHaveCSS("text-align", "center");
        await expect(second).toHaveCSS("text-align-last", "auto");

        // Third paragraph: inside a blockquote inside .center (not a direct child),
        // so the unset rule does not apply and local alignment is used.
        await expect(third).toHaveCSS("text-align", "justify");
        await expect(third).toHaveCSS("text-align-last", "start");
    },
);
