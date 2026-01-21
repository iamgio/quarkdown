import {suite} from "../../quarkdown";
import {getPageSizeTarget} from "../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies page margins correctly",
    ["plain", "paged"],
    async (page, docType) => {
        if (docType === "paged") {
            const target= getPageSizeTarget(page, docType);
            const marginTop = target.locator(".pagedjs_margin-top");
            const marginLeft = target.locator(".pagedjs_margin-left");

            const marginTopBox = await marginTop.boundingBox();
            const marginLeftBox = await marginLeft.boundingBox();
            expect(marginTopBox).not.toBeNull();
            expect(marginLeftBox).not.toBeNull();

            // Vertical margin: 50px
            expect(marginTopBox!.height).toBeCloseTo(50, 0);
            // Horizontal margin: 100px
            expect(marginLeftBox!.width).toBeCloseTo(100, 0);
        } else {
            const target = page.locator("body");
            await expect(target).toHaveCSS("margin-top", "50px");
            await expect(target).toHaveCSS("margin-bottom", "50px");
            await expect(target).toHaveCSS("margin-left", "100px");
            await expect(target).toHaveCSS("margin-right", "100px");
        }
    }
);
