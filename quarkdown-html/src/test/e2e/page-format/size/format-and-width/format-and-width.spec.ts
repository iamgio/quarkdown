import {suite} from "../../../quarkdown";
import {asPresentationSize, A5_HEIGHT_PX, A5_WIDTH_PX, getPageSizeTarget, getPresentationSizeTarget} from "../../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies default format with width override",
    ["paged", "slides"],
    async (page, docType) => {
        // Slides use the landscape orientation by default.
        if (docType === "slides") {
            const target = getPresentationSizeTarget(page);
            await expect(target).toHaveCSS("width", "100px");
            await expect(target).toHaveCSS("height", asPresentationSize(A5_WIDTH_PX));
            return;
        }

        const target = getPageSizeTarget(page, docType);
        await expect(target).toHaveCSS("width", "100px");

        const box = await target.boundingBox();
        expect(box).not.toBeNull();
        expect(box!.height).toBeCloseTo(A5_HEIGHT_PX, 0);
    }
);
