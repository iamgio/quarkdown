import {expect as playwrightExpect, Locator} from "@playwright/test";
import {suite} from "../quarkdown";

export type BoundingBox = NonNullable<Awaited<ReturnType<Locator["boundingBox"]>>>;

const {expect} = suite(__dirname);

export async function getChildBoxes(container: Locator, count: number): Promise<BoundingBox[]> {
    const children = container.locator("> p");
    await playwrightExpect(children).toHaveCount(count);

    const boxes = await Promise.all(
        Array.from({length: count}, (_, i) => children.nth(i).boundingBox())
    );
    return boxes as BoundingBox[];
}

export function assertHorizontalRow(boxes: BoundingBox[], gap: number) {
    for (let i = 1; i < boxes.length; i++) {
        const prevBoxRight = boxes[i - 1].x + boxes[i - 1].width;
        expect(boxes[i].x - prevBoxRight).toBeCloseTo(gap, 0);
    }
}

export function assertVerticalColumn(boxes: BoundingBox[], gap: number) {
    for (let i = 1; i < boxes.length; i++) {
        const prevBoxBottom = boxes[i - 1].y + boxes[i - 1].height;
        expect(boxes[i].y - prevBoxBottom).toBeCloseTo(gap, 0);
    }
}
