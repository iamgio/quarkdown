import {getComputedColor} from "../../__util/css";
import {assertMermaidBase, assertSvgWidthRatio} from "../index";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "renders pie chart correctly",
    ["plain", "paged", "slides"],
    async (page) => {
        const {pre, svg} = await assertMermaidBase(page);

        // Get computed RGB values for CSS variables
        const lineColor = await getComputedColor(page, "var(--qd-mermaid-node-line-color)");
        const bgColor = await getComputedColor(page, "var(--qd-mermaid-node-background-color)");

        // Legend text must have fill color --qd-mermaid-node-line-color
        const legendText = svg.locator(".legend text").first();
        await expect(legendText).toHaveCSS("fill", lineColor);

        // No path or rect should have fill color --qd-mermaid-node-background-color
        const paths = svg.locator("path");
        const rects = svg.locator("rect");

        const pathCount = await paths.count();
        for (let i = 0; i < pathCount; i++) {
            const fill = await paths.nth(i).evaluate((el) => getComputedStyle(el).fill);
            expect(fill).not.toBe(bgColor);
        }

        const rectCount = await rects.count();
        for (let i = 0; i < rectCount; i++) {
            const fill = await rects.nth(i).evaluate((el) => getComputedStyle(el).fill);
            expect(fill).not.toBe(bgColor);
        }

        // SVG width is between 60-70% of parent
        await assertSvgWidthRatio(pre, svg, 0.6, 0.7);
    }
);
