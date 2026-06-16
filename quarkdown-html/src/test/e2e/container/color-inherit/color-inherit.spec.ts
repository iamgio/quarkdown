import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("forces direct children to inherit the container color", async (page) => {
    const containers = page.locator(".container");
    const outerContainer = containers.nth(0);

    const red = await getComputedColor(page, "#ff0000");

    // Heading, paragraph and list inside the colored container all inherit red,
    // overriding their default theme colors.
    await expect(outerContainer.locator("h1")).toHaveCSS("color", red);
    await expect(outerContainer.locator("p")).toHaveCSS("color", red);
    await expect(outerContainer.locator("ul")).toHaveCSS("color", red);

    // The container itself carries the foreground color as an inline style.
    await expect(outerContainer).toHaveCSS("color", red);
});

test("preserves default theme colors outside of a colored container", async (page) => {
    const standaloneHeading = page.locator("h1").first();
    const standaloneParagraph = page.locator("p").first();

    const headingColor = await getComputedColor(page, "var(--qd-heading-color)");
    const mainColor = await getComputedColor(page, "var(--qd-main-color)");

    // The standalone heading keeps the theme's heading color, distinct from main text.
    await expect(standaloneHeading).toHaveCSS("color", headingColor);
    await expect(standaloneParagraph).toHaveCSS("color", mainColor);
    expect(headingColor).not.toBe(mainColor);
});

test("preserves the inner container color when nested", async (page) => {
    const containers = page.locator(".container");
    const innerContainer = containers.nth(2);

    const blue = await getComputedColor(page, "#0000ff");

    // The inner container's own foreground color is preserved,
    // not overridden by the outer container's color rule.
    await expect(innerContainer).toHaveCSS("color", blue);

    // Children of the inner container inherit blue, not the outer red.
    await expect(innerContainer.locator("h1")).toHaveCSS("color", blue);
    await expect(innerContainer.locator("p")).toHaveCSS("color", blue);
});
