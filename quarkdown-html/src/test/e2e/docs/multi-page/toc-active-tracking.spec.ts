import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("highlights currently viewed heading in toc", async (page) => {
    // Shrink viewport so the content area is small enough for headings
    // to scroll in and out of the observer's active zone.
    await page.setViewportSize({width: 1280, height: 300});

    const toc = page.locator('aside nav[data-role="table-of-contents"]');
    await expect(toc).toBeAttached();

    const items = toc.locator("li[data-target-id]");
    const count = await items.count();
    expect(count).toBeGreaterThan(1);

    const activeItems = toc.locator("li.active");

    // First item should be highlighted initially
    await expect(activeItems).toHaveCount(1);
    await expect(items.first()).toHaveClass(/active/);

    // Scroll the last heading to the top of the viewport
    const lastTargetId = await items.last().getAttribute("data-target-id");
    await page.evaluate((id) => document.getElementById(id!)?.scrollIntoView({block: "start"}), lastTargetId);

    // Last item should be highlighted
    await expect(activeItems).toHaveCount(1);
    await expect(items.last()).toHaveClass(/active/);
}, {subpath: "page-2"});
