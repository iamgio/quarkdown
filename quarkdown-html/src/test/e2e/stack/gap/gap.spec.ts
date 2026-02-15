import {suite} from "../../quarkdown";
import {assertHorizontalRow, assertVerticalColumn, getChildBoxes} from "../index";

const {test, expect} = suite(__dirname);

test("row applies horizontal gap between children", async (page) => {
    const expectedGap = 10;
    const row = page.locator(".stack.stack-row");
    const boxes = await getChildBoxes(row, 3);
    await expect(row).toHaveCSS("column-gap", `${expectedGap}px`);
    assertHorizontalRow(boxes, expectedGap);
});

test("column applies vertical gap between children", async (page) => {
    const expectedGap = 15;
    const column = page.locator(".stack.stack-column");
    const boxes = await getChildBoxes(column, 3);
    await expect(column).toHaveCSS("row-gap", `${expectedGap}px`);
    assertVerticalColumn(boxes, expectedGap);
});

test("grid applies gap between children in both directions", async (page) => {
    const expectedGap = 20;
    const grid = page.locator(".stack.stack-grid");
    const boxes = await getChildBoxes(grid, 3);
    await expect(grid).toHaveCSS("gap", `${expectedGap}px`);
    await expect(grid).toHaveCSS("row-gap", `${expectedGap}px`);
    await expect(grid).toHaveCSS("column-gap", `${expectedGap}px`);

    // First row: A, B
    assertHorizontalRow([boxes[0], boxes[1]], expectedGap);

    // Second row: C (below, aligned with A)
    assertVerticalColumn([boxes[0], boxes[2]], expectedGap);
});
