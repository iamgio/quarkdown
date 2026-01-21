import {suite} from "../../quarkdown";
import {getMarginContent, getPageContainers} from "../index";

const {testMatrix, expect} = suite(__dirname);

// Expected margins per page
const EXPECTED: Array<{topcenter?: string; bottomcenter?: string}> = [
    {bottomcenter: "A"},
    {topcenter: "B", bottomcenter: "A"},
    {topcenter: "B", bottomcenter: "C"},
];

testMatrix(
    "renders scoped page margins correctly",
    ["paged", "slides", "slides-print"],
    async (page, docType) => {
        const containers = getPageContainers(page, docType);
        await expect(containers).toHaveCount(3);

        for (let i = 0; i < 3; i++) {
            const container = containers.nth(i);
            const expected = EXPECTED[i];

            if (expected.topcenter) {
                const marginContent = getMarginContent(container, docType, "topcenter");
                await expect(marginContent).toBeAttached();
                await expect(marginContent).toHaveText(expected.topcenter);
            }

            if (expected.bottomcenter) {
                const marginContent = getMarginContent(container, docType, "bottomcenter");
                await expect(marginContent).toBeAttached();
                await expect(marginContent).toHaveText(expected.bottomcenter);
            }
        }
    }
);
