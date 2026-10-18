import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies style options to a row", async (page) => {
    const row = page.locator(".stack.stack-row");
    const red = await getComputedColor(page, "red");

    await expect(row).toHaveCSS("width", "300px");
    await expect(row).toHaveCSS("height", "100px");
    await expect(row).toHaveCSS("background-color", red);
    await expect(row).toHaveCSS("padding", "10px");
    await expect(row).toHaveCSS("column-gap", "10px");
    await expect(row).toHaveCSS("font-variant-caps", "small-caps");
});

test("applies border and text options to a column", async (page) => {
    const column = page.locator(".stack.stack-column");
    const blue = await getComputedColor(page, "blue");

    await expect(column).toHaveCSS("border-color", blue);
    await expect(column).toHaveCSS("border-width", "2px");
    await expect(column).toHaveCSS("border-style", "solid");
    await expect(column).toHaveCSS("border-radius", "8px");
    await expect(column).toHaveCSS("text-align", "center");
    await expect(column.locator("> p").first()).toHaveCSS("text-align", "center");
});

test("keeps the main-axis alignment of a styled grid", async (page) => {
    const grid = page.locator(".stack.stack-grid");
    const white = await getComputedColor(page, "white");
    const black = await getComputedColor(page, "black");

    await expect(grid).toHaveCSS("justify-content", "space-evenly");
    await expect(grid).toHaveCSS("color", white);
    await expect(grid).toHaveCSS("background-color", black);
    await expect(grid.locator("> p").first()).toHaveCSS("color", white);
});
