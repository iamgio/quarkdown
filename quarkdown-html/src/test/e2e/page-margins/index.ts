import {Locator, Page} from "@playwright/test";
import {
    MARGIN_TARGETS as DOCS_MARGIN_CONTAINERS
} from "../../../main/typescript/document/handlers/page-margins/page-margins-docs";

// Margin names
export const MARGIN_NAMES = [
    "topleftcorner",
    "topleft",
    "topcenter",
    "topright",
    "toprightcorner",
    "righttop",
    "rightmiddle",
    "rightbottom",
    "bottomrightcorner",
    "bottomright",
    "bottomcenter",
    "bottomleft",
    "bottomleftcorner",
    "leftbottom",
    "leftmiddle",
    "lefttop",
];

// Maps margin name to hyphenated suffix
export const MARGIN_SUFFIX: Record<string, string> = {
    topleftcorner: "top-left-corner",
    topleft: "top-left",
    topcenter: "top-center",
    topright: "top-right",
    toprightcorner: "top-right-corner",
    righttop: "right-top",
    rightmiddle: "right-middle",
    rightbottom: "right-bottom",
    bottomrightcorner: "bottom-right-corner",
    bottomright: "bottom-right",
    bottomcenter: "bottom-center",
    bottomleft: "bottom-left",
    bottomleftcorner: "bottom-left-corner",
    leftbottom: "left-bottom",
    leftmiddle: "left-middle",
    lefttop: "left-top",
};

// Maps hyphenated margin name to docs container selector
export const DOCS_CONTAINERS: Record<string, string> = DOCS_MARGIN_CONTAINERS;

/**
 * Gets the page/slide containers based on doctype.
 */
export function getPageContainers(page: Page, docType: string): Locator {
    return docType === "paged"
        ? page.locator(".pagedjs_page")
        : page.locator(".reveal .slide-background");
}

/**
 * Gets the margin content locator for a specific margin on a container.
 */
export function getMarginContent(container: Locator, docType: string, marginName: string): Locator {
    const suffix = MARGIN_SUFFIX[marginName];
    if (docType === "paged") {
        return container.locator(`.pagedjs_margin-${suffix} > .pagedjs_margin-content`);
    }
    return container.locator(`.page-margin-${suffix}.page-margin-content`);
}
