import {Page} from "@playwright/test";
import {suite} from "../../../quarkdown";
import {DocumentType} from "../../../__util/paths";

const {testMatrix, expect} = suite(__dirname);

// The source defines 5 slides:
// - Global: bordercolor:red, bordertop:4px
// - Range 2..3: background:blue, bordercolor:green
// - Right (odd) slides within 4..5: background:yellow, so only slide 5 matches.

const RED = "rgb(255, 0, 0)";
const GREEN = "rgb(0, 128, 0)";
const BLUE = "rgb(0, 0, 255)";
const YELLOW = "rgb(255, 255, 0)";

const DOC_TYPES: DocumentType[] = ["slides", "slides-print"];

function getBackgrounds(page: Page) {
    return page.locator(".reveal :is(.backgrounds, .slides > .pdf-page) > .slide-background");
}

testMatrix("slides inside the range have the scoped background", DOC_TYPES, async (page) => {
    const backgrounds = getBackgrounds(page);
    await expect(backgrounds).toHaveCount(5);

    await expect(backgrounds.nth(1)).toHaveCSS("background-color", BLUE);
    await expect(backgrounds.nth(2)).toHaveCSS("background-color", BLUE);
});

testMatrix("slides outside the range keep the global background", DOC_TYPES, async (page) => {
    const backgrounds = getBackgrounds(page);

    await expect(backgrounds.nth(0)).not.toHaveCSS("background-color", BLUE);
    await expect(backgrounds.nth(3)).not.toHaveCSS("background-color", BLUE);
    await expect(backgrounds.nth(4)).not.toHaveCSS("background-color", BLUE);
});

testMatrix("slides inside the range have the scoped border color", DOC_TYPES, async (page) => {
    const backgrounds = getBackgrounds(page);

    await expect(backgrounds.nth(1)).toHaveCSS("border-top-color", GREEN);
    await expect(backgrounds.nth(1)).toHaveCSS("border-top-width", "4px");
});

testMatrix("slides outside the range keep the global border color", DOC_TYPES, async (page) => {
    const backgrounds = getBackgrounds(page);

    await expect(backgrounds.nth(0)).toHaveCSS("border-top-color", RED);
    await expect(backgrounds.nth(3)).toHaveCSS("border-top-color", RED);
});

testMatrix("side restricts a range to odd or even slides", DOC_TYPES, async (page) => {
    const backgrounds = getBackgrounds(page);

    await expect(backgrounds.nth(3)).not.toHaveCSS("background-color", YELLOW);
    await expect(backgrounds.nth(4)).toHaveCSS("background-color", YELLOW);
});
