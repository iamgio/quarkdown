import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

const BOX_TYPES = ["callout", "tip", "note", "warning", "error"] as const;

test("renders box types with correct colors and icons", async (page) => {
    const boxes = page.locator(".box");
    await expect(boxes).toHaveCount(5);

    for (const type of BOX_TYPES) {
        const box = page.locator(`.box.${type}`);
        await expect(box).toBeAttached();

        const header = box.locator("> header");
        const content = box.locator("> .box-content");

        await expect(header).toBeAttached();
        await expect(content).toBeAttached();

        // Get expected colors for this box type
        const titleFgColor = await getComputedColor(page, `var(--qd-${type}-title-foreground-color)`);
        const contentFgColor = await getComputedColor(page, `var(--qd-${type}-content-foreground-color)`);
        const bgColor = await getComputedColor(page, `var(--qd-${type}-background-color)`);

        await expect(header).toHaveCSS("color", titleFgColor);
        await expect(box).toHaveCSS("background-color", bgColor);

        // Header background should be different from content background
        const headerBg = await header.evaluate((el) => getComputedStyle(el).backgroundColor);
        expect(headerBg).not.toBe(bgColor);

        // Content text color should match content foreground color
        await expect(content).toHaveCSS("color", contentFgColor);

        // Non-callout boxes have an icon in header > h4::before
        if (type !== "callout" && type !== "error") {
            const beforeContent = await header.locator("h4")
                .evaluate((el) => getComputedStyle(el, "::before").content);
            expect(beforeContent).not.toBe("none");
            expect(beforeContent).not.toBe('""');
        }
    }
});
