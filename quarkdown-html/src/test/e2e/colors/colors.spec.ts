import {getComputedColor} from "../__util/css";
import {suite} from "../quarkdown";

const {test, expect} = suite(__dirname);

test("applies theme colors correctly", async (page) => {
    const root = page.locator(":root");
    const heading = page.locator("h1").first();
    const text = page.locator("p").first();
    const link = page.locator("a").first();

    const colorScheme = await page.evaluate(() =>
        getComputedStyle(document.documentElement).getPropertyValue("--qd-color-scheme").trim()
    );
    await expect(root).toHaveCSS("color-scheme", colorScheme);
    expect(colorScheme).toBe("dark");

    const headingColor = await getComputedColor(page, "var(--qd-heading-color)");
    const mainColor = await getComputedColor(page, "var(--qd-main-color)");
    const linkColor = await getComputedColor(page, "var(--qd-link-color)");

    await expect(heading).toHaveCSS("color", headingColor);
    await expect(text).toHaveCSS("color", mainColor);
    await expect(link).toHaveCSS("color", linkColor);
});
