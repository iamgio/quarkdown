import {getComputedColor} from "../../__util/css";
import {assertMermaidBase, assertSvgWidthRatio} from "../index";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "renders class diagram correctly",
    ["plain", "paged", "slides"],
    async (page) => {
        const {pre, svg} = await assertMermaidBase(page);

        // Get computed RGB values for CSS variables
        const borderColor = await getComputedColor(page, "var(--qd-mermaid-node-border-color)");
        const bgColor = await getComputedColor(page, "var(--qd-mermaid-node-background-color)");

        // At least one path or text should have the expected colors
        const paths = svg.locator("path");
        const texts = svg.locator("text");

        let hasMatchingColor = false;

        const pathCount = await paths.count();
        for (let i = 0; i < pathCount && !hasMatchingColor; i++) {
            const stroke = await paths.nth(i).evaluate((el) => getComputedStyle(el).stroke);
            const fill = await paths.nth(i).evaluate((el) => getComputedStyle(el).fill);
            if (stroke === borderColor || fill === bgColor) {
                hasMatchingColor = true;
            }
        }

        const textCount = await texts.count();
        for (let i = 0; i < textCount && !hasMatchingColor; i++) {
            const stroke = await texts.nth(i).evaluate((el) => getComputedStyle(el).stroke);
            const fill = await texts.nth(i).evaluate((el) => getComputedStyle(el).fill);
            if (stroke === borderColor || fill === bgColor) {
                hasMatchingColor = true;
            }
        }

        expect(hasMatchingColor).toBe(true);

        // SVG width is between 50-60% of parent
        await assertSvgWidthRatio(pre, svg, 0.5, 0.6);
    }
);
