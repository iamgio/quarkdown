import {Locator, Page} from "@playwright/test";
import {DocumentType} from "../__util/paths";

// A4 dimensions: 210mm × 297mm
export const A4_WIDTH_PX = 210 * 96 / 25.4; // ~793.7px
export const A4_HEIGHT_PX = 297 * 96 / 25.4; // ~1122.52px

// A5 dimensions: 148mm × 210mm
export const A5_WIDTH_PX = 148 * 96 / 25.4; // ~559.37px
export const A5_HEIGHT_PX = 210 * 96 / 25.4; // ~793.7px

/**
 * In slides, the page size is not applied via CSS: it is forwarded to Reveal.js
 * as the presentation size, which Reveal applies in pixels to the slides container,
 * scaling it to fit the viewport while preserving the aspect ratio.
 * Since scaling happens via transform, the computed width/height stay the configured size.
 */
export function getPresentationSizeTarget(page: Page): Locator {
    return page.locator(".reveal .slides");
}

/**
 * @returns The pixel size Reveal.js is expected to apply, given a possibly fractional
 *          page size in pixels, as the runtime rounds it before initializing Reveal.
 */
export function asPresentationSize(pixels: number): string {
    return `${Math.round(pixels)}px`;
}

export function getPageSizeTarget(page: Page, docType: DocumentType): Locator {
    switch (docType) {
        case "plain":
            return page.locator("body > main");
        case "docs":
            return page.locator(".content-wrapper > main");
        case "slides":
        case "slides-print":
            return page.locator(".reveal");
        case "paged":
            return page.locator(".pagedjs_page").first();
        default:
            throw new Error(`Unsupported docType: ${docType}`);
    }
}
