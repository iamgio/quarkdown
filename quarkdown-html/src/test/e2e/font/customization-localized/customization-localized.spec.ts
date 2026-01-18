import {evaluateComputedStyle} from "../../__util/css";
import {fontFamilyMatches, getCssVar, isHashedFont} from "../index";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies custom fonts with localized fallback", async (page) => {
    const mainLocalizedFont = await getCssVar(page, "--qd-main-localized-font");
    const headingLocalizedFont = await getCssVar(page, "--qd-heading-localized-font");
    const codeFont = await getCssVar(page, "--qd-code-font");

    // Localized fonts should still be set to Noto Serif SC
    expect(mainLocalizedFont).toContain("Noto Serif SC");
    expect(headingLocalizedFont).toContain("Noto Serif SC");

    // Paragraph uses custom font (hashed) as primary
    const paragraph = page.locator("p");
    const pStyle = await evaluateComputedStyle(paragraph);
    expect(isHashedFont(pStyle.fontFamily)).toBe(true);
    // Localized font is included as fallback
    expect(pStyle.fontFamily).toContain("Noto Serif SC");

    // Heading uses custom font (hashed) as primary
    const h2 = page.locator("h2");
    const h2Style = await evaluateComputedStyle(h2);
    expect(isHashedFont(h2Style.fontFamily)).toBe(true);
    // Localized font is included as fallback
    expect(h2Style.fontFamily).toContain("Noto Serif SC");

    // Custom fonts are different from each other
    expect(pStyle.fontFamily).not.toBe(h2Style.fontFamily);

    // Code font is unchanged (not customized, not localized)
    const codeBlock = page.locator("pre code");
    const codeBlockStyle = await evaluateComputedStyle(codeBlock);
    expect(fontFamilyMatches(codeBlockStyle.fontFamily, codeFont)).toBe(true);
    expect(isHashedFont(codeBlockStyle.fontFamily)).toBe(false);
});
