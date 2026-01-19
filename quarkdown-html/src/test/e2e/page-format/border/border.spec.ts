import {suite} from "../../quarkdown";
import {getPageSizeTarget} from "../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies page border correctly",
    ["plain", "paged", "slides"],
    async (page, docType) => {
        let target = getPageSizeTarget(page, docType);
        if (docType === "paged") {
            target = target.locator(".pagedjs_area");
        }

        await expect(target).toHaveCSS("border-top-width", "30px");
        await expect(target).toHaveCSS("border-bottom-width", "5px");
        await expect(target).toHaveCSS("border-left-width", "10px");
        await expect(target).toHaveCSS("border-right-width", "40px");

        const color = "rgb(255, 0, 0)";
        await expect(target).toHaveCSS("border-top-color", color);
        await expect(target).toHaveCSS("border-bottom-color", color);
        await expect(target).toHaveCSS("border-left-color", color);
        await expect(target).toHaveCSS("border-right-color", color);
    }
);
