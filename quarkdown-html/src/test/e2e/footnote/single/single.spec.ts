import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "renders footnote in correct area",
    ["plain", "paged", "slides"],
    async (page, docType) => {
        const reference = page.locator(".footnote-reference");
        await expect(reference).toBeAttached();

        const definition = page.locator(".footnote-definition");
        await expect(definition).toBeAttached();

        switch (docType) {
            case "plain": {
                // Plain: footnotes are in the right margin area
                const marginArea = page.locator("#margin-area-right");
                await expect(marginArea.locator(".footnote-definition")).toBeAttached();

                // Footnote should be roughly aligned with reference
                const refBox = await reference.boundingBox();
                const defBox = await definition.boundingBox();
                expect(refBox).not.toBeNull();
                expect(defBox).not.toBeNull();
                expect(Math.abs(defBox!.y - refBox!.y)).toBeLessThanOrEqual(30);
                break;
            }
            case "paged": {
                // Paged: footnotes are in the paged.js footnote area
                const footnoteArea = page.locator(".pagedjs_area .pagedjs_footnote_area");
                await expect(footnoteArea.locator(".footnote-definition")).toBeAttached();

                // Footnote area should be in the bottom fourth of the page
                const pageBox = await page.locator(".pagedjs_page").boundingBox();
                const areaBox = await footnoteArea.boundingBox();
                expect(pageBox).not.toBeNull();
                expect(areaBox).not.toBeNull();
                const bottomFourthStart = pageBox!.y + pageBox!.height * 0.75;
                expect(areaBox!.y).toBeGreaterThanOrEqual(bottomFourthStart);

                expect(areaBox!.height).toBeGreaterThan(0);

                // Footnote area bottom should be close to the top of the bottom margin
                const marginBottom = page.locator(".pagedjs_margin-bottom");
                const marginBox = await marginBottom.boundingBox();
                expect(marginBox).not.toBeNull();
                const areaBottom = areaBox!.y + areaBox!.height;
                expect(areaBottom).toBeCloseTo(marginBox!.y, 1);
                break;
            }
            case "slides": {
                // Slides: footnotes are in a footnote-area at the bottom of the slide
                const footnoteArea = page.locator(".footnote-area");
                await expect(footnoteArea).toBeAttached();
                await expect(footnoteArea.locator(".footnote-definition")).toBeAttached();

                // Footnote area should be in the bottom half of the slide
                const slideBox = await page.locator(".reveal .slides section").first().boundingBox();
                const areaBox = await footnoteArea.boundingBox();
                expect(slideBox).not.toBeNull();
                expect(areaBox).not.toBeNull();
                const bottomHalfStart = slideBox!.y + slideBox!.height * 0.5;
                expect(areaBox!.y).toBeGreaterThanOrEqual(bottomHalfStart);
                break;
            }
        }
    }
);
