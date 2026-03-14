import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("sidebar contains only an empty ol", async (page) => {
    const sidebar = page.locator(".sidebar");
    await expect(sidebar).toBeAttached();

    // Sidebar should have exactly one direct child: an ol
    const children = sidebar.locator("> *");
    await expect(children).toHaveCount(1);

    const ol = sidebar.locator("> ol");
    await expect(ol).toBeAttached();

    // The ol should have no items
    await expect(ol.locator("li")).toHaveCount(0);
});
