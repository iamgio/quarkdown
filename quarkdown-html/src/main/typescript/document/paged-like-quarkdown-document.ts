import {QuarkdownDocument} from "./quarkdown-document";

/**
 * A page in a paged-like Quarkdown document.
 */
export type QuarkdownPage = {
    querySelectorAll(query: string): NodeListOf<HTMLElement>
};

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
     * @param includeDisplayNumbers - Whether to consider logical numbers, which may differ from physical page numbers,
     *                                e.g. caused by page number resets (default: `true`)
     * @returns The page number (1-based)
     */
    getPageNumber(page: TPage, includeDisplayNumbers?: boolean): number;

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

    /**
     * Sets the display page number for the given page.
     * The display number is a logical page number that may differ from the physical page number,
     * for example due to page number resets.
     * @param page - The page to set the display number for
     * @param pageNumber - The display page number to set (1-based)
     */
    setDisplayPageNumber(page: TPage, pageNumber: number): void;
}