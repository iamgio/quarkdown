import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

const DESKTOP_WIDTH = 1400;
const MOBILE_WIDTH = 600;
const TABLET_WIDTH = 1000;

testMatrix(
    "plain layout reflows at sm breakpoint",
    ["plain"],
    async (page) => {
        const body = page.locator("body");
        const main = body.locator("> main");
        const leftAside = body.locator("> aside#margin-area-left");
        const rightAside = body.locator("> aside#margin-area-right");

        // Desktop: side-by-side layout
        await page.setViewportSize({width: DESKTOP_WIDTH, height: 800});

        await expect(body).toHaveCSS("flex-direction", "row");
        await expect(leftAside).toBeVisible();

        const mainBoxDesktop = await main.boundingBox();
        const bodyBoxDesktop = await body.boundingBox();
        expect(mainBoxDesktop).not.toBeNull();
        expect(bodyBoxDesktop).not.toBeNull();
        expect(mainBoxDesktop!.width).toBeLessThan(bodyBoxDesktop!.width);

        // Mobile: stacked layout
        await page.setViewportSize({width: MOBILE_WIDTH, height: 800});

        await expect(body).toHaveCSS("flex-direction", "column");

        // Left aside is hidden on mobile
        await expect(leftAside).toHaveCSS("display", "none");

        // Right aside is visible and below main (for footnotes)
        await expect(rightAside).toHaveCSS("display", "block");
        const mainBoxForPosition = await main.boundingBox();
        const rightAsideBox = await rightAside.boundingBox();
        expect(mainBoxForPosition).not.toBeNull();
        expect(rightAsideBox).not.toBeNull();
        expect(rightAsideBox!.y).toBeGreaterThan(mainBoxForPosition!.y + mainBoxForPosition!.height - 1);

        // Main takes full width
        const mainBoxMobile = await main.boundingBox();
        const bodyBoxMobile = await body.boundingBox();
        expect(mainBoxMobile).not.toBeNull();
        expect(bodyBoxMobile).not.toBeNull();
        // Account for padding
        expect(mainBoxMobile!.width).toBeGreaterThan(bodyBoxMobile!.width * 0.9);
    }
);

testMatrix(
    "docs layout reflows at md breakpoint",
    ["docs"],
    async (page) => {
        const body = page.locator("body");
        const header = body.locator("> header");
        const contentWrapper = body.locator("> .content-wrapper");
        const headerAsideLeft = header.locator("> aside").first();
        const contentMain = contentWrapper.locator("> main");
        const contentLeftAside = contentWrapper.locator("> aside#margin-area-left");

        // Desktop: side-by-side layout
        await page.setViewportSize({width: DESKTOP_WIDTH, height: 800});

        await expect(contentWrapper).toHaveCSS("flex-direction", "row");
        await expect(headerAsideLeft).toBeVisible();

        // Content asides are sticky on desktop
        await expect(contentLeftAside).toHaveCSS("position", "sticky");

        const mainBoxDesktop = await contentMain.boundingBox();
        const wrapperBoxDesktop = await contentWrapper.boundingBox();
        expect(mainBoxDesktop).not.toBeNull();
        expect(wrapperBoxDesktop).not.toBeNull();
        expect(mainBoxDesktop!.width).toBeLessThan(wrapperBoxDesktop!.width);

        // Tablet (below md): stacked layout
        await page.setViewportSize({width: TABLET_WIDTH, height: 800});

        await expect(contentWrapper).toHaveCSS("flex-direction", "column");

        // Header asides are hidden on tablet
        await expect(headerAsideLeft).not.toBeVisible();

        // Content asides are static on tablet
        await expect(contentLeftAside).toHaveCSS("position", "static");

        // Content aside has border-top
        const borderTop = await contentLeftAside.evaluate((el) => getComputedStyle(el).borderTopWidth);
        expect(parseFloat(borderTop)).toBeGreaterThan(0);

        // Main takes more width
        const mainBoxTablet = await contentMain.boundingBox();
        const wrapperBoxTablet = await contentWrapper.boundingBox();
        expect(mainBoxTablet).not.toBeNull();
        expect(wrapperBoxTablet).not.toBeNull();
        expect(mainBoxTablet!.width).toBeGreaterThan(wrapperBoxTablet!.width * 0.9);
    }
);
