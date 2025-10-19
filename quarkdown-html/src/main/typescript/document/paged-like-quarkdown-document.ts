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
     * @returns The page number (1-based)
     */
    getPageNumber(page: TPage): number;

    /**
     * @param page - The page to get the type for
     * @returns The page type ('left' or 'right')
     */
    getPageType(page: TPage): 'left' | 'right';
}