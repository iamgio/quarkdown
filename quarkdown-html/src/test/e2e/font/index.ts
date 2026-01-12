import type {Page} from "@playwright/test";
import {expect} from "@playwright/test";
import {evaluateComputedStyle} from "../__util/css";

/**
 * Gets the computed value of a CSS custom property.
 */
export async function getCssVar(page: Page, varName: string): Promise<string> {
    return page.evaluate((name) => {
        return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    }, varName);
}

/**
 * Checks if a font family is a hashed custom font (starts with quoted numeric hash).
 */
export function isHashedFont(fontFamily: string): boolean {
    return /^"-?\d+/.test(fontFamily);
}

/**
 * Checks if the computed font family matches the expected font family,
 * ignoring quotes and case.
 */
export function fontFamilyMatches(computedFontFamily: string, expectedFontFamily: string): boolean {
    const cleanedComputed = computedFontFamily.replace(/['"]/g, "").toLowerCase();
    const cleanedExpected = expectedFontFamily.replace(/['"]/g, "").toLowerCase();
    return cleanedComputed.startsWith(cleanedExpected);
}

/**
 * Tests custom font application with configurable heading behavior.
 * @param page - Playwright page
 * @param expectCustomHeadings - Whether headings should use the custom font
 */
export async function testCustomFontApplication(page: Page, expectCustomHeadings: boolean): Promise<void> {
    const codeFont = await getCssVar(page, "--qd-code-font");

    // Paragraph uses custom font (hashed)
    const paragraph = page.locator("p");
    const pStyle = await evaluateComputedStyle(paragraph);
    expect(isHashedFont(pStyle.fontFamily)).toBe(true);

    // Heading font depends on theme configuration
    const h2 = page.locator("h2");
    const h2Style = await evaluateComputedStyle(h2);
    if (expectCustomHeadings) {
        expect(fontFamilyMatches(h2Style.fontFamily, pStyle.fontFamily)).toBe(true);
    } else {
        const headingFont = await getCssVar(page, "--qd-heading-font");
        expect(fontFamilyMatches(h2Style.fontFamily, headingFont)).toBe(true);
    }

    // Code block font is unchanged (still uses default code font)
    const codeBlock = page.locator("pre code");
    const codeBlockStyle = await evaluateComputedStyle(codeBlock);
    expect(fontFamilyMatches(codeBlockStyle.fontFamily, codeFont)).toBe(true);
    expect(codeBlockStyle.fontFamily).not.toBe(pStyle.fontFamily);
}
