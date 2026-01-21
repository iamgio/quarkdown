import {evaluateComputedStyle} from "../../__util/css";
import {fontFamilyMatches, getCssVar} from "../index";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const FALLBACK_HEADING_FONT = "Inter";
const FALLBACK_MAIN_FONT = "Lato";

test("sets localized font variables for zh locale", async (page) => {
    const mainLocalizedFont = await getCssVar(page, "--qd-main-localized-font");
    const headingLocalizedFont = await getCssVar(page, "--qd-heading-localized-font");

    // Localized fonts should be set to Noto Serif SC
    expect(mainLocalizedFont).toContain("Noto Serif SC");
    expect(headingLocalizedFont).toContain("Noto Serif SC");
});

test("applies localized fonts to Chinese text, base fonts to non-Chinese", async (page) => {
    const mainLocalizedFont = await getCssVar(page, "--qd-main-localized-font");
    const headingLocalizedFont = await getCssVar(page, "--qd-heading-localized-font");
    const codeFont = await getCssVar(page, "--qd-code-font");

    const headings = page.locator("h2");
    const paragraphs = page.locator("p");

    // Headings use localized font with fallback to base heading font
    const headingStyle = await evaluateComputedStyle(headings.nth(0));
    expect(fontFamilyMatches(
            headingStyle.fontFamily,
            headingLocalizedFont + ", " + headingLocalizedFont + ", " + FALLBACK_HEADING_FONT
        )
    ).toBe(true);

    // Main text uses localized font with fallback to base main font
    const pStyle = await evaluateComputedStyle(paragraphs.nth(0));
    expect(fontFamilyMatches(
        pStyle.fontFamily,
        mainLocalizedFont + ", " + mainLocalizedFont + ", " + FALLBACK_MAIN_FONT
    )).toBe(true);

    // Code font is unchanged (not localized)
    const codeBlock = page.locator("pre code");
    const codeBlockStyle = await evaluateComputedStyle(codeBlock);
    expect(fontFamilyMatches(codeBlockStyle.fontFamily, codeFont)).toBe(true);
});
