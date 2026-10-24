import {suite} from "../../quarkdown";
import {getPageSizeTarget} from "../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies content area padding correctly",
    ["plain", "paged", "slides", "slides-print"],
    async (page, docType) => {
        let target = getPageSizeTarget(page, docType);
        if (docType === "paged") {
            target = target.locator(".pagedjs_area");
        } else if (docType === "slides") {
            target = target.locator(".slides > section").first();
        } else if (docType === "slides-print") {
            target = target.locator(".slides > .pdf-page section").first();
        }

        await expect(target).toHaveCSS("padding-top", "20px");
        await expect(target).toHaveCSS("padding-bottom", "20px");
        await expect(target).toHaveCSS("padding-left", "40px");
        await expect(target).toHaveCSS("padding-right", "40px");
    }
);
