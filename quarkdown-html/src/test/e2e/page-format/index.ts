import {Locator, Page} from "@playwright/test";
import {DocumentType} from "../__util/paths";

// A4 dimensions: 210mm × 297mm
export const A4_WIDTH_PX = 210 * 96 / 25.4; // ~793.7px
export const A4_HEIGHT_PX = 297 * 96 / 25.4; // ~1122.52px

// A5 dimensions: 148mm × 210mm
export const A5_WIDTH_PX = 148 * 96 / 25.4; // ~559.37px
export const A5_HEIGHT_PX = 210 * 96 / 25.4; // ~793.7px

export function getPageSizeTarget(page: Page, docType: DocumentType): Locator {
    switch (docType) {
        case "plain":
            return page.locator("body > main");
        case "slides":
            return page.locator(".reveal");
        case "paged":
            return page.locator(".pagedjs_page").first();
        default:
            throw new Error(`Unsupported docType: ${docType}`);
    }
}
