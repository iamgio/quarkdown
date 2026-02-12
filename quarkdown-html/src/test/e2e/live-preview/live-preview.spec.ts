import {expect, test} from "@playwright/test";
import * as fs from "fs";
import * as path from "path";
import {DocumentType} from "../__util/paths";
import {runLivePreviewTest} from "../__util/live-preview-runner";

const TEST_DIR = __dirname;

/** Timeout for each individual test (compilation + reload cycles are slow). */
const TEST_TIMEOUT = 90_000;

/** Timeout for waiting for reloaded content to appear after an edit. */
const RELOAD_TIMEOUT = 30_000;

const DOC_TYPES: DocumentType[] = ["plain", "paged", "slides", "docs"];

/**
 * Reads the original main.qd content (without doctype) for building edited versions.
 */
function readOriginalMain(): string {
    return fs.readFileSync(path.join(TEST_DIR, "main.qd"), "utf-8");
}

/**
 * Returns the original main.qd content with "Marker Alpha" replaced by a different marker.
 */
function mainWithMarker(marker: string): string {
    return readOriginalMain().replace("Marker Alpha", marker);
}

// --- Source file edit triggers reload ---

for (const docType of DOC_TYPES) {
    test(`source edit triggers reload [${docType}]`, async ({page}) => {
        test.setTimeout(TEST_TIMEOUT);

        await runLivePreviewTest(
            TEST_DIR,
            page,
            async (ctx) => {
                // Verify initial content is loaded.
                await expect(ctx.activeFrame.locator("body")).toContainText("Marker Alpha", {
                    timeout: RELOAD_TIMEOUT,
                });

                // Edit main.qd with a new marker.
                ctx.editFile("main.qd", mainWithMarker("Marker Beta"));

                // The reload should eventually show the new marker.
                await expect(ctx.activeFrame.locator("body")).toContainText("Marker Beta", {
                    timeout: RELOAD_TIMEOUT,
                });
            },
            {docType},
        );
    });
}

// --- Subdocument edit triggers reload ---

for (const docType of DOC_TYPES) {
    test(`subdocument edit triggers reload [${docType}]`, async ({page}) => {
        test.setTimeout(TEST_TIMEOUT);

        await runLivePreviewTest(
            TEST_DIR,
            page,
            async (ctx) => {
                // Verify initial subdocument content is loaded.
                await expect(ctx.activeFrame.locator("body")).toContainText("Marker Gamma", {
                    timeout: RELOAD_TIMEOUT,
                });

                // Edit sub.qd with a new marker.
                ctx.editFile("sub.qd", "Marker Delta\n");

                // The reload should eventually show the new marker.
                await expect(ctx.activeFrame.locator("body")).toContainText("Marker Delta", {
                    timeout: RELOAD_TIMEOUT,
                });
            },
            {docType, subpath: "sub/index.html"},
        );
    });
}

// --- Scroll position preserved across reload ---
// Slides use Reveal.js which handles scrolling internally, so window.scrollY doesn't apply.
const SCROLLABLE_DOC_TYPES: DocumentType[] = ["plain", "paged", "docs"];

for (const docType of SCROLLABLE_DOC_TYPES) {
    test(`scroll position preserved across reload [${docType}]`, async ({page}) => {
        test.setTimeout(TEST_TIMEOUT);

        await runLivePreviewTest(
            TEST_DIR,
            page,
            async (ctx) => {
                // Wait for initial content.
                await expect(ctx.activeFrame.locator("body")).toContainText("Marker Alpha", {
                    timeout: RELOAD_TIMEOUT,
                });

                // Scroll the active iframe to a specific position.
                const visibleIframe = page.locator("iframe.visible");
                const iframeHandle = await visibleIframe.elementHandle({timeout: 5000});
                const contentFrame = await iframeHandle!.contentFrame();

                // Scroll down and wait for it to settle.
                await contentFrame!.evaluate(() => window.scrollTo(0, 400));
                await page.waitForTimeout(500);

                // Verify scroll position was applied.
                const scrollBefore = await contentFrame!.evaluate(() => window.scrollY);
                expect(scrollBefore).toBeGreaterThan(100);

                // Edit main.qd to trigger a reload.
                ctx.editFile("main.qd", mainWithMarker("Marker Beta"));

                // Wait for the reload to complete.
                await expect(ctx.activeFrame.locator("body")).toContainText("Marker Beta", {
                    timeout: RELOAD_TIMEOUT,
                });

                // Check scroll position on the now-active iframe (may have swapped).
                const newIframeHandle = await page.locator("iframe.visible").elementHandle({timeout: 5000});
                const newContentFrame = await newIframeHandle!.contentFrame();
                const scrollAfter = await newContentFrame!.evaluate(() => window.scrollY);

                // Scroll position should be approximately preserved (within tolerance).
                expect(scrollAfter).toBeGreaterThan(100);
                expect(Math.abs(scrollAfter - scrollBefore)).toBeLessThan(200);
            },
            {docType},
        );
    });
}
