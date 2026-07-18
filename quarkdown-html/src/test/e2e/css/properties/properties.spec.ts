import {suite} from "../../quarkdown";

const {test, testMatrix, expect} = suite(__dirname);

test("applies CSS property overrides", async (page) => {
    const link = page.getByRole("link", {name: "Link"});

    await expect(link).toBeAttached();
    await expect(link).toHaveCSS("color", "rgb(255, 0, 0)");
});

testMatrix(
    "does not create a blank page before the table of contents",
    ["paged"],
    async (page) => {
        const pages = page.locator(".pagedjs_page");

        await expect(pages).toHaveCount(2);
        await expect(pages.first().getByRole("link", {name: "Heading"})).toHaveCSS("color", "rgb(255, 0, 0)");
    }
);
