import {evaluateComputedStyle, getComputedSizeProperty} from "../../__util/css";
import {SM_WIDTH} from "../../__util/breakpoints";
import {fontFamilyMatches, getCssVar} from "../index";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

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
        const captionFontSize = await getComputedSizeProperty(page, "var(--qd-caption-font-size)");

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

        // Table caption uses caption font size
        const tableCaption = page.locator("table caption");
        const tableCaptionStyle = await evaluateComputedStyle(tableCaption);
        expect(parseFloat(tableCaptionStyle.fontSize)).toBeCloseTo(captionFontSize, 1);

        // Figure caption (code block) uses caption font size
        const figCaption = page.locator("figure figcaption");
        const figCaptionStyle = await evaluateComputedStyle(figCaption);
        expect(parseFloat(figCaptionStyle.fontSize)).toBeCloseTo(captionFontSize, 1);
    }
);

testMatrix(
    "applies correct font sizes on small screens",
    ["plain", "paged", "slides", "docs"],
    async (page, docType) => {
        await page.setViewportSize({width: SM_WIDTH, height: 800});

        const isSmResponsive = docType === "plain" || docType === "docs";

        // Code block font size
        const codeBlockParent = page.locator("pre");
        const codeBlock = codeBlockParent.locator("code");
        const codeBlockStyle = await evaluateComputedStyle(codeBlock);
        const actualCodeBlockFontSize = parseFloat(codeBlockStyle.fontSize);
        const smCodeBlockFontSize = await getComputedSizeProperty(codeBlockParent, "var(--qd-sm-code-block-font-size)");

        if (isSmResponsive) {
            expect(actualCodeBlockFontSize).toBeCloseTo(smCodeBlockFontSize, 1);
        } else {
            expect(actualCodeBlockFontSize).not.toBeCloseTo(smCodeBlockFontSize, 1);
        }

        // Caption font size (table caption and figure caption)
        const tableCaption = page.locator("table caption");
        const figCaption = page.locator("figure figcaption");
        const tableCaptionStyle = await evaluateComputedStyle(tableCaption);
        const figCaptionStyle = await evaluateComputedStyle(figCaption);
        const smCaptionFontSize = await getComputedSizeProperty(page, "var(--qd-sm-caption-font-size)");

        if (isSmResponsive) {
            expect(parseFloat(tableCaptionStyle.fontSize)).toBeCloseTo(smCaptionFontSize, 1);
            expect(parseFloat(figCaptionStyle.fontSize)).toBeCloseTo(smCaptionFontSize, 1);
        } else {
            const captionFontSize = await getComputedSizeProperty(page, "var(--qd-caption-font-size)");
            expect(parseFloat(tableCaptionStyle.fontSize)).toBeCloseTo(captionFontSize, 1);
            expect(parseFloat(figCaptionStyle.fontSize)).toBeCloseTo(captionFontSize, 1);
        }
    }
);
