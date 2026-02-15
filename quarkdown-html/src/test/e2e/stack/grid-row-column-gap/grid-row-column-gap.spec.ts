import {suite} from "../../quarkdown";
import {assertHorizontalRow, assertVerticalColumn, getChildBoxes} from "../index";

const {test, expect} = suite(__dirname);

const EXPECTED_GAPS = [
    ["grid applies row gap", 10, undefined],
    ["grid applies column gap", undefined, 15],
    ["grid applies row gap and column gap", 20, 25],
    ["grid row gap defaults to gap", 30, 35],
    ["grid column gap defaults to gap", 45, 40],
    ["the order of row|column-gap and gap is irrelevant", 50, 55]
];

for (const {index, testName, expectedRowGap, expectedColumnGap} of EXPECTED_GAPS
    .map(([testName, expectedRowGap, expectedColumnGap], index) => ({
        index,
        testName: testName as string,
        expectedRowGap: expectedRowGap as number | undefined,
        expectedColumnGap: expectedColumnGap as number | undefined
    }))) {
    test(testName, async (page) => {
        const grid = page.locator(".stack.stack-grid").nth(index);
        const boxes = await getChildBoxes(grid, 3);
        if (expectedRowGap !== undefined) {
            await expect(grid).toHaveCSS("row-gap", `${expectedRowGap}px`);
        }
        if (expectedColumnGap !== undefined) {
            await expect(grid).toHaveCSS("column-gap", `${expectedColumnGap}px`);
        }

        // First row: A, B
        assertHorizontalRow([boxes[0], boxes[1]], expectedColumnGap || 0);

        // Second row: C (below, aligned with A)
        assertVerticalColumn([boxes[0], boxes[2]], expectedRowGap || 0);
    });
}
