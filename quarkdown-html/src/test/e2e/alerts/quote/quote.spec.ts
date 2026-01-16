import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

const ALERT_TYPES = ["tip", "note", "warning", "important"] as const;

testMatrix("renders blockquote alerts with correct colors",
    ["plain", "slides"],
    async (page) => {
        const blockquotes = page.locator("blockquote");
        await expect(blockquotes).toHaveCount(4);

        for (let i = 0; i < ALERT_TYPES.length; i++) {
            const type = ALERT_TYPES[i];
            const blockquote = blockquotes.nth(i);

            await expect(blockquote).toBeAttached();

            const headerFgColor = await getComputedColor(page, `var(--qd-${type}-title-foreground-color)`);
            await expect(blockquote).toHaveCSS("border-color", headerFgColor);

            // p::before should have content "Type: " with same color
            const p = blockquote.locator("p").first();
            const beforeContent = await p.evaluate((el) => getComputedStyle(el, "::before").content);
            const expectedContent = `"${type.charAt(0).toUpperCase() + type.slice(1)}: "`;
            expect(beforeContent).toBe(expectedContent);

            const beforeColor = await p.evaluate((el) => getComputedStyle(el, "::before").color);
            expect(beforeColor).toBe(headerFgColor);
        }
    }
);
