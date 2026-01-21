import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "renders multiple footnotes correctly",
    ["plain", "paged", "slides", "docs"],
    async (page, docType) => {
        const definitions = page.locator(".footnote-definition");
        await expect(definitions).toHaveCount(4);

        switch (docType) {
            case "plain": {
                const first = definitions.nth(0);
                const second = definitions.nth(1);
                const third = definitions.nth(2);
                const fourth = definitions.nth(3);

                const firstBox = await first.boundingBox();
                const secondBox = await second.boundingBox();
                const thirdBox = await third.boundingBox();
                const fourthBox = await fourth.boundingBox();

                expect(firstBox).not.toBeNull();
                expect(secondBox).not.toBeNull();
                expect(thirdBox).not.toBeNull();
                expect(fourthBox).not.toBeNull();

                // First, Second, Third should be stacked vertically
                const firstBottom = firstBox!.y + firstBox!.height;
                const secondBottom = secondBox!.y + secondBox!.height;
                const thirdBottom = thirdBox!.y + thirdBox!.height;

                expect(secondBox!.y).toBeCloseTo(firstBottom, 0);
                expect(thirdBox!.y).toBeCloseTo(secondBottom, 0);

                // Distance between consecutive footnotes should be equal
                const dist12 = secondBox!.y - firstBottom;
                const dist23 = thirdBox!.y - secondBottom;
                expect(dist12).toBeCloseTo(dist23, 0);

                // Distance to fourth should be greater (different paragraph)
                const dist34 = fourthBox!.y - thirdBottom;
                expect(dist34).toBeGreaterThan(dist12);
                break;
            }
            case "paged": {
                const footnoteArea = page.locator(".pagedjs_area .pagedjs_footnote_area");
                await expect(footnoteArea.locator(".footnote-definition")).toHaveCount(4);
                break;
            }
            case "slides": {
                const footnoteArea = page.locator(".footnote-area");
                await expect(footnoteArea).toBeAttached();
                await expect(footnoteArea.locator(".footnote-definition")).toHaveCount(4);
                break;
            }
            case "docs": {
                const footnoteArea = page.locator("#footnote-area");
                await expect(footnoteArea.locator(".footnote-definition")).toHaveCount(4);
                break;
            }
        }
    }
);
