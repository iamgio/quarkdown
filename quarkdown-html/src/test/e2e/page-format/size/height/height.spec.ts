import {suite} from "../../../quarkdown";
import {A4_WIDTH_PX, getPageSizeTarget} from "../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies page height to correct element",
    ["paged", "slides"],
    async (page, docType) => {
        const target = getPageSizeTarget(page, docType);

        await expect(target).toHaveCSS("height", "100px");

        // In paged, unset width defaults to A4 width
        if (docType === "paged") {
            const box = await target.boundingBox();
            expect(box).not.toBeNull();
            expect(box!.width).toBeCloseTo(A4_WIDTH_PX, 0);
        }
    }
);
