import {evaluateComputedStyle, getComputedSizeProperty} from "../../__util/css";
import {suite} from "../../quarkdown";
import {Page} from "@playwright/test";

const {testMatrix, expect} = suite(__dirname);

/**
 * Gets the computed value of a CSS custom property.
 */
async function getCssVar(page: Page, varName: string): Promise<string> {
    return page.evaluate((name) => {
        return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    }, varName);
}

/**
 * Checks if the computed font family matches the expected font family,
 * ignoring quotes and case.
 */
function fontFamilyMatches(computedFontFamily: string, expectedFontFamily: string): boolean {
    const cleanedComputed = computedFontFamily.replace(/['"]/g, "").toLowerCase();
    const cleanedExpected = expectedFontFamily.replace(/['"]/g, "").toLowerCase();
    return cleanedComputed.startsWith(cleanedExpected);
}

testMatrix(
    "applies correct default fonts",
    ["plain", "paged", "slides"],
    async (page) => {
        const mainFont = await getCssVar(page, "--qd-main-font");
        const headingFont = await getCssVar(page, "--qd-heading-font");
        const boxHeadingFont = await getCssVar(page, "--qd-box-heading-font");
        const codeFont = await getCssVar(page, "--qd-code-font");

        // Paragraph uses main font
        const paragraph = page.locator("p").first();
        const pStyle = await evaluateComputedStyle(paragraph);
        expect(fontFamilyMatches(pStyle.fontFamily, mainFont)).toBe(true);

        // Table uses main font
        const tableCell = page.locator("td").first();
        const tdStyle = await evaluateComputedStyle(tableCell);
        expect(fontFamilyMatches(tdStyle.fontFamily, mainFont)).toBe(true);

        // Box content uses main font
        const boxContent = page.locator(".box p").first();
        const boxContentStyle = await evaluateComputedStyle(boxContent);
        expect(fontFamilyMatches(boxContentStyle.fontFamily, mainFont)).toBe(true);

        // Heading (h2) uses heading font
        const h2 = page.locator("h2");
        const h2Style = await evaluateComputedStyle(h2);
        expect(fontFamilyMatches(h2Style.fontFamily, headingFont)).toBe(true);

        // Box header (h4) uses box heading font
        const boxHeader = page.locator(".box h4");
        const boxHeaderStyle = await evaluateComputedStyle(boxHeader);
        expect(fontFamilyMatches(boxHeaderStyle.fontFamily, boxHeadingFont)).toBe(true);

        // Code span uses code font
        const codeSpan = page.locator("code").first();
        const codeSpanStyle = await evaluateComputedStyle(codeSpan);
        expect(fontFamilyMatches(codeSpanStyle.fontFamily, codeFont)).toBe(true);

        // Code block uses code font
        const codeBlock = page.locator("pre code");
        const codeBlockStyle = await evaluateComputedStyle(codeBlock);
        expect(fontFamilyMatches(codeBlockStyle.fontFamily, codeFont)).toBe(true);
    }
);

testMatrix(
    "applies correct default font sizes",
    ["plain", "paged", "slides", "docs"],
    async (page, docType) => {
        const mainFontSize = await getComputedSizeProperty(page, "var(--qd-main-font-size)");
        const codeSpanFontSize = await getComputedSizeProperty(page, "var(--qd-code-span-font-size)");
        const codeBlockFontSize = await getComputedSizeProperty(page, "var(--qd-code-block-font-size)");

        // Paragraph uses main font size
        const paragraph = page.locator("p").first();
        const pStyle = await evaluateComputedStyle(paragraph);
        expect(parseFloat(pStyle.fontSize)).toBeCloseTo(mainFontSize, 1);

        // Code span uses code span font size
        const codeSpan = page.locator("p code").first();
        const codeSpanStyle = await evaluateComputedStyle(codeSpan);
        expect(parseFloat(codeSpanStyle.fontSize)).toBeCloseTo(codeSpanFontSize, 1);

        // Code block uses code block font size, or slides code block font size in slides
        const codeBlockParent = page.locator("pre");
        const codeBlock = codeBlockParent.locator("code");
        const slidesCodeBlockFontSize = await getComputedSizeProperty(codeBlockParent, "var(--qd-slides-code-block-font-size)");
        const codeBlockStyle = await evaluateComputedStyle(codeBlock);
        expect(parseFloat(codeBlockStyle.fontSize)).toBeCloseTo(
            docType === "slides" ? slidesCodeBlockFontSize : codeBlockFontSize,
            docType === "slides" ? 0 : 1,
        );
    }
);
