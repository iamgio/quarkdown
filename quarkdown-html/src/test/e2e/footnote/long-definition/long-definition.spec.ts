import {suite} from "../../quarkdown";
import {overflowingTexts} from "../../__util/paged";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "reserves the space a long footnote really takes on its page",
    ["paged"],
    async (page) => {
        const definition = page.locator(".footnote-definition");
        await expect(definition).toHaveCount(1);

        const placement = await definition.evaluate(element => {
            const pageElement = element.closest<HTMLElement>(".pagedjs_page")!;
            const reference = document.querySelector(`.footnote-reference[data-definition="${element.id}"]`)!;
            const area = pageElement.querySelector<HTMLElement>(".pagedjs_area")!;
            return {
                samePage: reference.closest(".pagedjs_page") === pageElement,
                reservedHeight: parseFloat(area.style.getPropertyValue("--pagedjs-footnotes-height")),
                definitionHeight: element.getBoundingClientRect().height,
            };
        });
        expect(placement.samePage).toBe(true);
        expect(placement.reservedHeight).toBeGreaterThanOrEqual(placement.definitionHeight);
        expect(await overflowingTexts(page)).toEqual([]);
    },
);

testMatrix(
    "keeps a long link inside the footnote area",
    ["paged"],
    async (page) => {
        const link = page.locator(".footnote-definition a");
        const linkBox = (await link.boundingBox())!;
        const areaBox = (await page.locator(".pagedjs_footnote_area").first().boundingBox())!;
        expect(linkBox.x + linkBox.width).toBeLessThanOrEqual(areaBox.x + areaBox.width + 1);
    },
);
