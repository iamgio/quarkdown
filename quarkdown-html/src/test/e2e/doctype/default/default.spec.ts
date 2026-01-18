import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "renders correct body classes and background",
    ["plain", "paged", "slides", "docs"],
    async (page, docType) => {
        const body = page.locator("body");

        // Body has correct classes
        await expect(body).toHaveClass(/quarkdown/);
        await expect(body).toHaveClass(new RegExp(`quarkdown-${docType}`));

        const bgColor = await getComputedColor(page, "var(--qd-background-color)");

        if (docType === "paged") {
            // Paged: body has different background, pages have --qd-background-color
            const bodyBg = await body.evaluate((el) => getComputedStyle(el).backgroundColor);
            expect(bodyBg).not.toBe(bgColor);

            const pagedPage = page.locator(".pagedjs_page");
            await expect(pagedPage).toHaveCSS("background-color", bgColor);
        } else {
            // Non-paged: body has --qd-background-color
            await expect(body).toHaveCSS("background-color", bgColor);
        }
    }
);

testMatrix(
    "renders plain layout correctly",
    ["plain"],
    async (page) => {
        const body = page.locator("body");
        const main = body.locator("> main");
        const leftAside = body.locator("> aside#margin-area-left");
        const rightAside = body.locator("> aside#margin-area-right");

        await expect(main).toBeAttached();
        await expect(leftAside).toBeAttached();
        await expect(rightAside).toBeAttached();

        // Content width is limited
        const mainBox = await main.boundingBox();
        const bodyBox = await body.boundingBox();
        expect(mainBox).not.toBeNull();
        expect(bodyBox).not.toBeNull();
        expect(mainBox!.width).toBeLessThan(bodyBox!.width);

        // Margins are at left and right of content
        const leftBox = await leftAside.boundingBox();
        const rightBox = await rightAside.boundingBox();
        expect(leftBox).not.toBeNull();
        expect(rightBox).not.toBeNull();
        expect(leftBox!.x).toBeLessThan(mainBox!.x);
        expect(rightBox!.x).toBeGreaterThan(mainBox!.x + mainBox!.width - 1);
    }
);

testMatrix(
    "renders docs layout correctly",
    ["docs"],
    async (page) => {
        const body = page.locator("body");
        const header = body.locator("> header");
        const contentWrapper = body.locator("> .content-wrapper");
        const contentMain = contentWrapper.locator("> main");
        const leftAside = contentWrapper.locator("> aside#margin-area-left");
        const rightAside = contentWrapper.locator("> aside#margin-area-right");

        await expect(header).toBeAttached();
        await expect(contentWrapper).toBeAttached();
        await expect(contentMain).toBeAttached();
        await expect(leftAside).toBeAttached();
        await expect(rightAside).toBeAttached();

        // Search bar is present in header
        const searchInput = header.locator("#search-input");
        await expect(searchInput).toBeAttached();

        // Content width is limited
        const mainBox = await contentMain.boundingBox();
        const wrapperBox = await contentWrapper.boundingBox();
        expect(mainBox).not.toBeNull();
        expect(wrapperBox).not.toBeNull();
        expect(mainBox!.width).toBeLessThan(wrapperBox!.width);

        // Header > main is aligned with content > main
        const headerMain = header.locator("> main");
        const headerMainBox = await headerMain.boundingBox();
        expect(headerMainBox).not.toBeNull();
        expect(headerMainBox!.x).toBeCloseTo(mainBox!.x, -1);

        // Header margins are aligned with content margins
        const headerLeftAside = header.locator("> aside").first();
        const headerRightAside = header.locator("> aside").last();
        const headerLeftBox = await headerLeftAside.boundingBox();
        const headerRightBox = await headerRightAside.boundingBox();
        const contentLeftBox = await leftAside.boundingBox();
        const contentRightBox = await rightAside.boundingBox();

        expect(headerLeftBox).not.toBeNull();
        expect(headerRightBox).not.toBeNull();
        expect(contentLeftBox).not.toBeNull();
        expect(contentRightBox).not.toBeNull();
        expect(headerLeftBox!.x).toBeCloseTo(contentLeftBox!.x, -1);
        expect(headerRightBox!.x).toBeCloseTo(contentRightBox!.x, -1);
    }
);

testMatrix(
    "renders paged layout correctly",
    ["paged"],
    async (page) => {
        const pages = page.locator(".pagedjs_pages");
        const pagedPage = page.locator(".pagedjs_page");

        await expect(pages).toBeAttached();
        await expect(pagedPage).toHaveCount(1);

        // Content is in correct location
        const contentDiv = pagedPage.locator(".pagedjs_area > .pagedjs_page_content > div");
        await expect(contentDiv).toBeAttached();

        // Content contains the heading
        const heading = contentDiv.locator("h1");
        await expect(heading).toBeAttached();
        await expect(heading).toHaveText("Page");
    }
);

testMatrix(
    "renders slides layout correctly",
    ["slides"],
    async (page) => {
        const reveal = page.locator(".reveal");
        const slides = reveal.locator("> .slides");
        const section = slides.locator("> section");
        const backgrounds = reveal.locator("> .backgrounds");
        const slideBackground = backgrounds.locator("> .slide-background");

        await expect(reveal).toBeAttached();
        await expect(slides).toBeAttached();
        await expect(section).toHaveCount(1);
        await expect(backgrounds).toBeAttached();
        await expect(slideBackground).toHaveCount(1);

        // Section contains the content
        const heading = section.locator("h1");
        await expect(heading).toBeAttached();
        await expect(heading).toHaveText("Page");

        // Background is empty (no page margin content in this test)
        const bgChildren = await slideBackground.locator("> * > *").count();
        expect(bgChildren).toBe(0);
    }
);
