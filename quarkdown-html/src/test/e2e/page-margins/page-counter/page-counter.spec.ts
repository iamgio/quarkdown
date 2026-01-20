import {suite} from "../../quarkdown";
import {getMarginContent, getPageContainers} from "../index";

const {testMatrix, expect} = suite(__dirname);

const EXPECTED_TEXTS = [
    "1 / 4",
    "2 / 4",
    "3 / 4",
    "20 / 4"
];

testMatrix(
    "renders page counter correctly",
    ["paged", "slides", "slides-print"],
    async (page, docType) => {
        const containers = getPageContainers(page, docType);
        await expect(containers).toHaveCount(4);

        for (let i = 0; i < 4; i++) {
            const container = containers.nth(i);
            const marginContent = getMarginContent(container, docType, "bottomcenter");
            await expect(marginContent).toBeAttached();
            await expect(marginContent).toHaveText(EXPECTED_TEXTS[i]);
        }
    }
);
