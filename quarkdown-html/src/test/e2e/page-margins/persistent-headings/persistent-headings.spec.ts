import {suite} from "../../quarkdown";
import {getMarginContent, getPageContainers} from "../index";

const {testMatrix, expect} = suite(__dirname);

// Expected content per page: [topleft, topcenter, topright]
const EXPECTED_HEADINGS: [string, string, string][] = [
    ["1", "", ""],
    ["1", "", ""],
    ["1", "1.1", ""],
    ["1", "1.1", ""],
    ["1", "1.1", "1.1.1"],
    ["1", "1.1", "1.1.1"],
    ["1", "1.2", ""],
    ["1", "1.2", "1.2.1"],
    ["2", "", ""],
];

const MARGINS = ["topleft", "topcenter", "topright"] as const;

testMatrix(
    "displays persistent headings in page margins",
    ["paged", "slides"],
    async (page, docType) => {
        const pages = getPageContainers(page, docType);
        await expect(pages).toHaveCount(EXPECTED_HEADINGS.length);

        for (let i = 0; i < EXPECTED_HEADINGS.length; i++) {
            const pageContainer = pages.nth(i);
            const expected = EXPECTED_HEADINGS[i];

            for (let j = 0; j < MARGINS.length; j++) {
                const marginContent = getMarginContent(pageContainer, docType, MARGINS[j]);
                const expectedText = expected[j];

                if (expectedText) {
                    await expect(marginContent).toHaveText(expectedText);
                } else {
                    await expect(marginContent).toBeEmpty();
                }
            }
        }
    }
);
