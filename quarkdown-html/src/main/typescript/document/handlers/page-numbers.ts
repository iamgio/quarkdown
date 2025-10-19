import {DocumentHandler} from "../document-handler";
import {PagedLikeQuarkdownDocument, QuarkdownPage} from "../paged-like-quarkdown-document";

/**
 * Abstract base class for document handlers that manage page numbering.
 * Provides utility methods to find and update page number elements in documents.
 */
export class PageNumbers extends DocumentHandler<PagedLikeQuarkdownDocument<any>> {
    /**
     * Gets all elements that display the total page count.
     * @returns NodeList of total page number elements (`.total-page-number`)
     */
    private getTotalPageNumberElements(): NodeListOf<HTMLElement> {
        return document.querySelectorAll<HTMLElement>('.total-page-number');
    }

    /**
     * Gets all elements that display the current page number.
     * @param page - The page element to search within
     * @returns NodeList of current page number elements (`.current-page-number`)
     */
    private getCurrentPageNumberElements(page: QuarkdownPage): NodeListOf<HTMLElement> {
        return page.querySelectorAll('.current-page-number');
    }

    /**
     * Updates all total page number elements with the total count of pages.
     */
    private updateTotalPageNumbers(pages: QuarkdownPage[]) {
        const amount = pages.length;
        this.getTotalPageNumberElements().forEach(total => {
            total.innerText = amount.toString();
        });
    }

    /**
     * Updates all current page number elements with their respective page indices.
     */
    private updateCurrentPageNumbers(pages: QuarkdownPage[]) {
        pages.forEach(page => {
            const number = this.quarkdownDocument.getPageNumber(page);
            this.getCurrentPageNumberElements(page).forEach(pageNumber => {
                pageNumber.innerText = number.toString();
            });
        });
    }

    /**
     * Updates both total and current page numbers after rendering completes.
     */
    async onPostRendering() {
        const pages = this.quarkdownDocument.getPages();
        this.updateTotalPageNumbers(pages);
        this.updateCurrentPageNumbers(pages);
    }
}