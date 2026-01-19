import {suite} from "../../../quarkdown";
import {A4_HEIGHT_PX, getPageSizeTarget} from "../../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies page width to correct element",
    ["plain", "paged", "slides"],
    async (page, docType) => {
        const target = getPageSizeTarget(page, docType);

        await expect(target).toHaveCSS("width", "100px");

        // In paged, unset height defaults to A4 height
        if (docType === "paged") {
            const box = await target.boundingBox();
            expect(box).not.toBeNull();
            expect(box!.height).toBeCloseTo(A4_HEIGHT_PX, 0);
        }

        // In plain print mode, width expands to 100%
        if (docType === "plain") {
            await page.emulateMedia({media: "print"});

            const body = page.locator("body");
            const bodyBox = await body.boundingBox();
            const targetBox = await target.boundingBox();
            expect(bodyBox).not.toBeNull();
            expect(targetBox).not.toBeNull();
            expect(targetBox!.width).toBeCloseTo(bodyBox!.width, -2);
        }
    }
);
