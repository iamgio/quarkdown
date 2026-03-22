import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("suppresses child margin when container has explicit margin", async (page) => {
    const figures = page.locator("figure");
    const firstFigure = figures.nth(0);
    const secondFigure = figures.nth(1);
    const containers = page.locator(".container");
    const outerContainer = containers.nth(0);

    // The standalone figure retains its default non-zero margins.
    await expect(firstFigure).not.toHaveCSS("margin-top", "0px");

    // The figure inside a container with explicit margin has its margins suppressed.
    await expect(secondFigure).toHaveCSS("margin-top", "0px");
    await expect(secondFigure).toHaveCSS("margin-bottom", "0px");

    // The container itself has the 20px margin set via inline style.
    await expect(outerContainer).toHaveCSS("margin", "20px");
});

test("preserves inner container margin when nested", async (page) => {
    const containers = page.locator(".container");
    const innerContainer = containers.nth(2);

    // The inner container's own inline margin (10px) is preserved,
    // not suppressed by the outer container's margin rule.
    await expect(innerContainer).toHaveCSS("margin", "10px");
});
