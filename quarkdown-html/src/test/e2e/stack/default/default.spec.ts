import {expect as playwrightExpect, Locator} from "@playwright/test";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

type BoundingBox = NonNullable<Awaited<ReturnType<Locator["boundingBox"]>>>;

async function getChildBoxes(container: Locator, count: number): Promise<BoundingBox[]> {
    const children = container.locator("> p");
    await playwrightExpect(children).toHaveCount(count);

    const boxes = await Promise.all(
        Array.from({length: count}, (_, i) => children.nth(i).boundingBox())
    );
    return boxes as BoundingBox[];
}

function assertHorizontalRow(boxes: BoundingBox[]) {
    for (let i = 1; i < boxes.length; i++) {
        // Same Y
        expect(boxes[i].y).toBeCloseTo(boxes[0].y, 0);
        // Starts where previous ends
        expect(boxes[i - 1].x + boxes[i - 1].width).toBeCloseTo(boxes[i].x, 0);
    }
}

function assertVerticalColumn(boxes: BoundingBox[]) {
    for (let i = 1; i < boxes.length; i++) {
        // Same X
        expect(boxes[i].x).toBeCloseTo(boxes[0].x, -1);
        // Starts where previous ends
        expect(boxes[i - 1].y + boxes[i - 1].height).toBeCloseTo(boxes[i].y, 0);
    }
}

test("row positions children horizontally with 0px gap", async (page) => {
    const boxes = await getChildBoxes(page.locator(".stack.stack-row"), 3);
    assertHorizontalRow(boxes);
});

test("column positions children vertically with 0px gap", async (page) => {
    const boxes = await getChildBoxes(page.locator(".stack.stack-column"), 3);
    assertVerticalColumn(boxes);
});

test("grid positions children in 2-column layout", async (page) => {
    const boxes = await getChildBoxes(page.locator(".stack.stack-grid"), 3);

    // First row: A, B
    assertHorizontalRow([boxes[0], boxes[1]]);

    // Second row: C (below, aligned with A)
    assertVerticalColumn([boxes[0], boxes[2]]);
});

test("stack elements have correct margins", async (page) => {
    const stacks = page.locator(".stack");

    for (const stack of await stacks.all()) {
        const marginTop = await stack.evaluate((el) => parseFloat(getComputedStyle(el).marginTop));
        const marginBottom = await stack.evaluate((el) => parseFloat(getComputedStyle(el).marginBottom));

        expect(marginTop).toBeGreaterThan(0);
        expect(marginBottom).toBeGreaterThan(0);
        expect(marginTop).toBeCloseTo(marginBottom, 0);
    }
});

test("paragraphs inside stacks have 0 margin", async (page) => {
    const paragraphs = page.locator(".stack > p");

    for (const p of await paragraphs.all()) {
        await expect(p).toHaveCSS("margin", "0px");
    }
});
