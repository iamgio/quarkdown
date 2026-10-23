import {evaluateComputedStyle} from "../../__util/css";
import {isHashedFont} from "../index";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

// The beamer layout defines its own base fonts, which must still yield to custom fonts.
testMatrix(
    "applies custom main, heading and code fonts on a layout that defines its own base fonts",
    ["plain", "paged", "slides"],
    async (page) => {
        const pStyle = await evaluateComputedStyle(page.locator("p").first());
        const h2Style = await evaluateComputedStyle(page.locator("h2").first());
        const codeStyle = await evaluateComputedStyle(page.locator("pre code").first());

        expect(isHashedFont(pStyle.fontFamily)).toBe(true);
        expect(isHashedFont(h2Style.fontFamily)).toBe(true);
        expect(isHashedFont(codeStyle.fontFamily)).toBe(true);

        expect(pStyle.fontFamily).not.toBe(h2Style.fontFamily);
        expect(pStyle.fontFamily).not.toBe(codeStyle.fontFamily);
    }
);
