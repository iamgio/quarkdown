import {expect, Locator, Page} from "@playwright/test";

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
 * Asserts that a heading's background either bleeds symmetrically past both
 * slide section edges, or stays within them.
 * @param heading - Playwright locator for a visible heading in a slide
 * @param bleeds - Whether the heading is expected to bleed past the edges
 */
async function expectHeadingBleed(heading: Locator, bleeds: boolean): Promise<void> {
    await expect(heading).toBeVisible();
    const {left, right} = await getHeadingBleed(heading);

    if (bleeds) {
        expect(left).toBeGreaterThan(0);
        expect(right).toBeGreaterThan(0);
        expect(right).toBeCloseTo(left, 0);
    } else {
        expect(left).toBeLessThanOrEqual(1);
        expect(right).toBeLessThanOrEqual(1);
    }
}

/**
 * Checks the extension of the theme's heading backgrounds relative to the slide edges:
 * the h1 on the first slide, then the h2 on the second slide, which always bleeds.
 * @param page - Playwright page displaying the theme's slides document
 * @param h1Bleeds - Whether the h1 background is expected to bleed past the slide edges
 */
export async function checkHeadingExtension(page: Page, h1Bleeds: boolean): Promise<void> {
    await expectHeadingBleed(page.locator("h1"), h1Bleeds);

    await page.keyboard.press("ArrowRight");
    await expectHeadingBleed(page.locator("h2"), true);
}
