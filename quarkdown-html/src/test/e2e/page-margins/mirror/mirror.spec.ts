import {suite} from "../../quarkdown";
import {getMarginContent, getPageContainers} from "../index";

const {testMatrix, expect} = suite(__dirname);

// Mirror margins: inside/outside swap based on odd/even pages
// Odd pages (1, 3): inside = left, outside = right
// Even pages (2, 4): inside = right, outside = left
const MIRROR_MARGINS: Record<string, {odd: string; even: string}> = {
    topoutsidecorner: {odd: "toprightcorner", even: "topleftcorner"},
    topoutside: {odd: "topright", even: "topleft"},
    topinsidecorner: {odd: "topleftcorner", even: "toprightcorner"},
    topinside: {odd: "topleft", even: "topright"},
    bottomoutsidecorner: {odd: "bottomrightcorner", even: "bottomleftcorner"},
    bottomoutside: {odd: "bottomright", even: "bottomleft"},
    bottominsidecorner: {odd: "bottomleftcorner", even: "bottomrightcorner"},
    bottominside: {odd: "bottomleft", even: "bottomright"},
};

testMatrix(
    "renders mirrored page margins correctly",
    ["paged", "slides", "slides-print"],
    async (page, docType) => {
        const containers = getPageContainers(page, docType);
        await expect(containers).toHaveCount(4);

        for (const [mirrorName, positions] of Object.entries(MIRROR_MARGINS)) {
            for (let i = 0; i < 4; i++) {
                const container = containers.nth(i);
                const isOdd = (i + 1) % 2 === 1;
                const resolvedPosition = isOdd ? positions.odd : positions.even;

                const marginContent = getMarginContent(container, docType, resolvedPosition);
                await expect(marginContent).toBeAttached();
                await expect(marginContent).toHaveText(mirrorName);
            }
        }
    }
);
