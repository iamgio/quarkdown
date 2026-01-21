import {suite} from "../../../quarkdown";
import {getPageSizeTarget} from "../../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies both width and height",
    ["paged", "slides"],
    async (page, docType) => {
        const target = getPageSizeTarget(page, docType);

        await expect(target).toHaveCSS("width", "100px");
        await expect(target).toHaveCSS("height", "100px");
    }
);
