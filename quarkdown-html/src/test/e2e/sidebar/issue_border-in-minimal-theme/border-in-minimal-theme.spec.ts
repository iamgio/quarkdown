import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("sidebar and children have no border in minimal theme", async (page) => {
    const sidebar = page.locator(".sidebar");
    await expect(sidebar).toBeAttached();

    // Collect sidebar and all nested children
    const allElements = sidebar.locator("*");
    const count = await allElements.count();

    // Sidebar itself should have no border
    await expect(sidebar).toHaveCSS("border-top-style", "none");
    await expect(sidebar).toHaveCSS("border-right-style", "none");
    await expect(sidebar).toHaveCSS("border-bottom-style", "none");
    await expect(sidebar).toHaveCSS("border-left-style", "none");

    // All children should have no border
    for (let i = 0; i < count; i++) {
        const el = allElements.nth(i);
        await expect(el).toHaveCSS("border-top-style", "none");
        await expect(el).toHaveCSS("border-right-style", "none");
        await expect(el).toHaveCSS("border-bottom-style", "none");
        await expect(el).toHaveCSS("border-left-style", "none");
    }
});
