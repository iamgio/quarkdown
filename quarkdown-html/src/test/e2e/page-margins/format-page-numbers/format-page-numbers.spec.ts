import {suite} from "../../quarkdown";
import {getMarginContent, getPageContainers} from "../index";

const {testMatrix, expect} = suite(__dirname);

const EXPECTED_TEXTS = [
    "1",
    "ii",
    "xx"
];

testMatrix(
    "renders formatted page numbers correctly",
    ["paged", "slides", "slides-print"],
    async (page, docType) => {
        const containers = getPageContainers(page, docType);
        await expect(containers).toHaveCount(3);

        for (let i = 0; i < 3; i++) {
            const container = containers.nth(i);
            const marginContent = getMarginContent(container, docType, "bottomcenter");
            await expect(marginContent).toBeAttached();
            await expect(marginContent).toHaveText(EXPECTED_TEXTS[i]);
        }
    }
);
