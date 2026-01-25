import {getComputedColor} from "../__util/css";
import {suite} from "../quarkdown";

const {testMatrix, expect} = suite(__dirname);

// sm breakpoint is 800px
const MOBILE_WIDTH = 600;

testMatrix(
    "renders sidebar correctly per doctype",
    ["plain", "paged", "slides", "slides-print", "docs"],
    async (page, docType) => {
        const sidebar = page.locator(".sidebar");

        if (docType === "slides" || docType === "slides-print" || docType === "docs") {
            // Slides and docs don't have sidebar
            await expect(sidebar).not.toBeAttached();
            return;
        }

        // Plain and paged have sidebar
        await expect(sidebar).toBeAttached();

        // Should have 7 items: A, B, C, D, E, F, G
        // Skipped: h4 (#### Skipped) and decorative (##! Skipped)
        const items = sidebar.locator("li");
        await expect(items).toHaveCount(7);

        // Verify no "Skipped" text appears in sidebar
        const sidebarText = await sidebar.textContent();
        expect(sidebarText).not.toContain("Skipped");

        // Check dash widths hierarchy: h1 > h2 > h3
        const h1Dash = sidebar.locator('li[data-depth="1"] > p').first();
        const h2Dash = sidebar.locator('li[data-depth="2"] > p').first();
        const h3Dash = sidebar.locator('li[data-depth="3"] > p').first();

        const h1Width = await h1Dash.evaluate((el) => {
            const after = getComputedStyle(el, "::after");
            return parseFloat(after.width);
        });
        const h2Width = await h2Dash.evaluate((el) => {
            const after = getComputedStyle(el, "::after");
            return parseFloat(after.width);
        });
        const h3Width = await h3Dash.evaluate((el) => {
            const after = getComputedStyle(el, "::after");
            return parseFloat(after.width);
        });

        expect(h1Width).toBeGreaterThan(h2Width);
        expect(h2Width).toBeGreaterThan(h3Width);

        // Check colors based on doctype
        const dashColor = await h1Dash.evaluate((el) => {
            return getComputedStyle(el, "::after").backgroundColor;
        });
        const mainColor = await getComputedColor(page, "var(--qd-main-color)");
        if (docType === "paged") {
            expect(dashColor).not.toBe(mainColor);
        } else {
            expect(dashColor).toBe(mainColor);
        }
    }
);

testMatrix(
    "hides sidebar on mobile in plain doctype",
    ["plain"],
    async (page) => {
        await page.setViewportSize({width: MOBILE_WIDTH, height: 800});

        const sidebar = page.locator(".sidebar");
        await expect(sidebar).toBeAttached();
        await expect(sidebar).toHaveCSS("display", "none");
    }
);

testMatrix(
    "hides sidebar in print mode",
    ["plain", "paged"],
    async (page) => {
        const sidebar = page.locator(".sidebar");
        await expect(sidebar).toBeAttached();

        await page.emulateMedia({media: "print"});
        await expect(sidebar).toHaveCSS("display", "none");
    }
);

testMatrix(
    "highlights currently viewed heading",
    ["paged"],
    async (page) => {
        // Setting viewport size to height of a single page
        const pageBox = await page.locator(".pagedjs_page").first().boundingBox();
        expect(pageBox).not.toBeNull();
        await page.setViewportSize({width: Math.floor(pageBox!.width), height: Math.floor(pageBox!.height)});

        const sidebar = page.locator(".sidebar");
        const items = sidebar.locator("li");
        const activeItems = sidebar.locator("li.active");

        // Only first item should be highlighted initially
        await expect(activeItems).toHaveCount(1);
        await expect(items.first()).toHaveClass(/active/);

        // Scroll to end of document
        await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));

        // Only second to last item should be highlighted
        await expect(activeItems).toHaveCount(1);
        await expect(items.nth(5)).toHaveClass(/active/);
    }
);
