import {suite} from "../../../quarkdown";
import {asPresentationSize, A5_HEIGHT_PX, A5_WIDTH_PX, getPageSizeTarget, getPresentationSizeTarget} from "../../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies A5 format with width override",
    ["paged", "slides"],
    async (page, docType) => {
        // Slides use the landscape orientation by default.
        if (docType === "slides") {
            const target = getPresentationSizeTarget(page);
            await expect(target).toHaveCSS("width", asPresentationSize(A5_HEIGHT_PX));
            await expect(target).toHaveCSS("height", asPresentationSize(A5_WIDTH_PX));
            return;
        }

        const box = await getPageSizeTarget(page, docType).boundingBox();
        expect(box).not.toBeNull();
        expect(box!.width).toBeCloseTo(A5_WIDTH_PX, 0);
        expect(box!.height).toBeCloseTo(A5_HEIGHT_PX, 0);
    }
);
