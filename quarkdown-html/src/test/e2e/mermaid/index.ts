import {expect, Page} from "@playwright/test";

/**
 * Common assertions for Mermaid diagrams.
 * Returns the pre and svg locators for additional assertions.
 */
export async function assertMermaidBase(page: Page) {
    const pre = page.locator("pre.mermaid");
    await expect(pre).toBeAttached();

    // Has data-processed attribute
    await expect(pre).toHaveAttribute("data-processed", "true");

    // Has inlined width: 100%
    const style = await pre.getAttribute("style");
    expect(style).toContain("width: 100%");

    // Only child is svg
    const svg = pre.locator("> svg");
    await expect(svg).toBeAttached();
    await expect(pre.locator("> *")).toHaveCount(1);

    // SVG has id mermaid-HASH
    const svgId = await svg.getAttribute("id");
    expect(svgId).toMatch(/^mermaid-[-\w]+$/i);

    return {pre, svg};
}

/**
 * Asserts that the SVG width is within a given percentage range of its parent.
 */
export async function assertSvgWidthRatio(
    pre: ReturnType<Page["locator"]>,
    svg: ReturnType<Page["locator"]>,
    min: number,
    max: number
) {
    const preBox = await pre.boundingBox();
    const svgBox = await svg.boundingBox();
    expect(preBox).not.toBeNull();
    expect(svgBox).not.toBeNull();
    const widthRatio = svgBox!.width / preBox!.width;
    expect(widthRatio).toBeGreaterThanOrEqual(min);
    expect(widthRatio).toBeLessThanOrEqual(max);
}
