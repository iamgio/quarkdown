import {suite} from "../quarkdown";

const {test, expect} = suite(__dirname);

test("renders icons correctly", async (page) => {
    // All icons have the bi class and specific icon class
    await expect(page.locator(".bi-heart")).toHaveCount(2);
    await expect(page.locator(".bi-star")).toHaveCount(1);
    await expect(page.locator(".bi-github")).toHaveCount(1);

    // Regular icons have the same size
    const listIcons = page.locator("li .icon-image");
    await expect(listIcons).toHaveCount(3);
    const listIconSizes = await listIcons.evaluateAll((icons) =>
        icons.map((icon) => parseFloat(getComputedStyle(icon).fontSize))
    );
    expect(new Set(listIconSizes).size).toBe(1);

    // Heading icon is larger than other icons
    const headingIcon = page.locator("h2 .icon-image");
    const headingIconSize = await headingIcon.evaluate((icon) =>
        parseFloat(getComputedStyle(icon).fontSize)
    );
    expect(headingIconSize).toBeGreaterThan(listIconSizes[0]);
});
