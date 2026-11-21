import {QuarkdownDocument} from "./quarkdown-document";

/**
 * A page in a paged-like Quarkdown document.
 */
export type QuarkdownPage = {
    querySelectorAll(query: string): NodeListOf<HTMLElement>
    getBoundingClientRect(): DOMRect
};

/**
 * Size of a page, in CSS pixels.
 */
export type PageSize = { width: number, height: number };

/**
 * @param document - The document to check
 * @returns Whether the document is divided into discrete pages
 */
export function isPagedLike(document: QuarkdownDocument): document is PagedLikeQuarkdownDocument<QuarkdownPage> {
    return 'getPages' in document;
}

/**
 * Measures the size of a page of the document, assuming all pages share the same size.
 * @param document - The document to measure
 * @returns The size of the first page, or `undefined` if the document is not paged or has no pages yet
 */
export function getPageSize(document: QuarkdownDocument): PageSize | undefined {
    if (!isPagedLike(document)) return undefined;
    const rect = document.getPages()[0]?.getBoundingClientRect();
    return rect && {width: rect.width, height: rect.height};
}

/**
 * A Quarkdown document that is divided into discrete pages.
 * @template TPage - The type of page elements in the document
 */
export interface PagedLikeQuarkdownDocument<TPage extends QuarkdownPage = HTMLElement> extends QuarkdownDocument {
    /**
     * Gets all pages in the document.
     * @returns All page elements.
     */
    getPages(): TPage[];

    /**
     * Gets the page number of the given page.
     * @param page - The page to get the number for
     * @returns The page number (1-based)
     */
    getPageNumber(page: TPage): number;

    /**
     * Gets the display page number of the given page, which may differ from the physical page number,
     * for example due to page number resets.
     * @param page - The page to get the display number for
     * @returns The display page number (1-based)
     */
    getDisplayPageNumber(page: TPage): string;

    /**
     * Sets the display page number for the given page.
     * The display number is a logical page number that may differ from the physical page number,
     * for example due to page number resets.
     * @param page - The page to set the display number for
     * @param pageNumber - The display page number to set (1-based)
     */
    setDisplayPageNumber(page: TPage, pageNumber: string): void;

    /**
     * @param page - The page to get the type for
     * @returns The page type ('left' or 'right')
     */
    getPageType(page: TPage): 'left' | 'right';

    /**
     * Gets the page that contains the given element.
     * @param element - The element to find the page for
     * @returns The containing page, or `undefined` if not found
     */
    getPage(element: HTMLElement): TPage | undefined;
}