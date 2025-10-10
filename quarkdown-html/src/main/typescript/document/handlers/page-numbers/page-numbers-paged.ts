import {PageNumbersDocumentHandler} from "./page-numbers-document-handler";

/**
 * Page numbering handler for paged documents.
 * Updates page counters to reflect the current paged position and total page count.
 */
export class PageNumbersPaged extends PageNumbersDocumentHandler {
    /**
     * Updates all total page number elements with the total count of pages.
     */
    private updateTotalPageNumbers(pages: NodeListOf<HTMLElement>) {
        const amount = pages.length;
        this.getTotalPageNumberElements().forEach(total => {
            total.innerText = amount.toString();
        });
    }

    /**
     * Updates all current page number elements with their respective page indices.
     */
    private updateCurrentPageNumbers(pages: NodeListOf<HTMLElement>) {
        pages.forEach(page => {
            const number = page.dataset.pageNumber;
            if (!number) return;
            this.getCurrentPageNumberElements(page).forEach(pageNumber => {
                pageNumber.innerText = number.toString();
            });
        });
    }

    /**
     * Updates both total and current page numbers after rendering completes.
     */
    onPostRendering() {
        const pages = document.querySelectorAll<HTMLElement>('.pagedjs_page')
        this.updateTotalPageNumbers(pages);
        this.updateCurrentPageNumbers(pages);
    }
}