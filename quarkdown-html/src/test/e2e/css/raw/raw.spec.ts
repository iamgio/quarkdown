import {suite} from "../../quarkdown";

const {test, testMatrix, expect} = suite(__dirname);

test("applies raw CSS styles", async (page) => {
    const link = page.getByRole("link", {name: "Link"});

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
