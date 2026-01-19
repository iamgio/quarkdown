import {suite} from "../../../quarkdown";
import {A5_HEIGHT_PX, A5_WIDTH_PX, getPageSizeTarget} from "../../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies A5 format with width override",
    ["paged", "slides"],
    async (page, docType) => {
        const target = getPageSizeTarget(page, docType);
        const isPortrait = docType === "paged";

        const box = await target.boundingBox();
        expect(box).not.toBeNull();
        expect(box!.width).toBeCloseTo(isPortrait ? A5_WIDTH_PX : A5_HEIGHT_PX, 0);
        expect(box!.height).toBeCloseTo(isPortrait ? A5_HEIGHT_PX : A5_WIDTH_PX, 0);
    }
);
