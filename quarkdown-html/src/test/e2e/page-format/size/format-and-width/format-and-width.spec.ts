import {suite} from "../../../quarkdown";
import {A5_HEIGHT_PX, A5_WIDTH_PX, getPageSizeTarget} from "../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies default format with width override",
    ["paged", "slides"],
    async (page, docType) => {
        const target = getPageSizeTarget(page, docType);
        const isPortrait = docType === "paged";

        await expect(target).toHaveCSS("width", "100px");

        const box = await target.boundingBox();
        expect(box).not.toBeNull();
        expect(box!.height).toBeCloseTo(isPortrait ? A5_HEIGHT_PX : A5_WIDTH_PX, 0);
    }
);
