import {expect, Locator, Page} from "@playwright/test";

/**
 * Expected properties of a theme's slide heading.
 */
export interface HeadingExpectations {
    /**
     * Whether the heading's background is expected to bleed past both slide edges.
     * If `undefined`, the extension is not checked.
     */
    bleeds?: boolean;
    /** Whether the heading is expected to keep a non-zero top margin. */
    hasTopMargin: boolean;
}

/**
 * Measures how far a heading extends past its enclosing slide section
 * on each side, in pixels. Positive values mean the heading bleeds past
 * the section edge; negative values mean it ends within the section.
 * @param heading - Playwright locator for a visible heading in a slide
 * @returns The left and right bleed distances
 */
async function getHeadingBleed(heading: Locator): Promise<{left: number; right: number}> {
    return heading.evaluate((el) => {
        const section = el.closest("section")!.getBoundingClientRect();
        const rect = el.getBoundingClientRect();
        return {
            left: section.x - rect.x,
            right: rect.x + rect.width - (section.x + section.width),
        };
    });
}

/**
 * Asserts that a visible heading matches the given expectations.
 * @param heading - Playwright locator for a visible heading in a slide
 * @param expectations - Expected properties of the heading
 */
async function checkHeading(heading: Locator, expectations: HeadingExpectations): Promise<void> {
    await expect(heading).toBeVisible();

    if (expectations.hasTopMargin) {
        await expect(heading).not.toHaveCSS("margin-top", "0px");
    } else {
        await expect(heading).toHaveCSS("margin-top", "0px");
    }

    if (expectations.bleeds === undefined) return;

    const {left, right} = await getHeadingBleed(heading);
    if (expectations.bleeds) {
        expect(left).toBeGreaterThan(0);
        expect(right).toBeGreaterThan(0);
        expect(right).toBeCloseTo(left, 0);
    } else {
        expect(left).toBeLessThanOrEqual(1);
        expect(right).toBeLessThanOrEqual(1);
    }
}

/**
 * Measures, in a single snapshot, the x-positions where the current slide's
 * h2 and paragraph text content start. A single evaluation keeps the two
 * measurements consistent while the slide transition is still animating.
 * @param page - Playwright page displaying a slides document
 * @returns The text start positions of the current slide's h2 and paragraph
 */
async function getTextStarts(page: Page): Promise<{heading: number; content: number}> {
    return page.evaluate(() => {
        const textStart = (el: Element) => {
            const range = document.createRange();
            range.selectNodeContents(el);
            return range.getBoundingClientRect().x;
        };
        const section = document.querySelector(".reveal .slides > section.present")!;
        return {
            heading: textStart(section.querySelector("h2")!),
            content: textStart(section.querySelector("p")!),
        };
    });
}

/**
 * Checks the theme's slide headings against the given expectations:
 * the h1 on the first slide, then the h2 on the second slide.
 * A bleeding h2 banner must also have its text start aligned with the
 * slide content, as its start padding compensates the negative margin.
 * @param page - Playwright page displaying the theme's slides document
 * @param h1 - Expected properties of the h1 heading
 * @param h2 - Expected properties of the h2 heading
 */
export async function checkHeadings(
    page: Page,
    h1: HeadingExpectations,
    h2: HeadingExpectations,
): Promise<void> {
    await checkHeading(page.locator("h1"), h1);

    await page.keyboard.press("ArrowRight");
    await checkHeading(page.locator("h2"), h2);

    if (h2.bleeds) {
        const {heading: headingTextStart, content: contentTextStart} = await getTextStarts(page);
        expect(headingTextStart).toBeCloseTo(contentTextStart, 0);
    }
}
