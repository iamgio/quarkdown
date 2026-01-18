import {getComputedColor} from "../../__util/css";
import {assertMermaidBase, assertSvgWidthRatio} from "../index";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "renders xy chart correctly",
    ["plain", "paged", "slides"],
    async (page) => {
        const {pre, svg} = await assertMermaidBase(page);

        // Get computed RGB values for CSS variables
        const linkColor = await getComputedColor(page, "var(--qd-link-color)");
        const borderColor = await getComputedColor(page, "var(--qd-mermaid-node-border-color)");

        // Line plot stroke matches --qd-link-color
        const linePlotPath = svg.locator(".line-plot-0 > path");
        await expect(linePlotPath).toHaveCSS("stroke", linkColor);

        // Axis elements match --qd-border-color
        const axisPath = svg.locator(":is(.left-axis, .bottom-axis) path").first();
        const axisText = svg.locator(":is(.left-axis, .bottom-axis) text").first();
        await expect(axisPath).toHaveCSS("stroke", borderColor);
        await expect(axisText).toHaveCSS("fill", borderColor);

        // SVG width is between 60-80% of parent
        await assertSvgWidthRatio(pre, svg, 0.6, 0.8);
    }
);
