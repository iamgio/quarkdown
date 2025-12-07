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
     * Finds all page number reset markers contained in the given page.
     */
    private getPageNumberResetMarkers(page: QuarkdownPage): HTMLElement[] {
        return Array.from(page.querySelectorAll('.page-number-reset'));
    }

    /**
     * Computes the logical page number for each page, applying reset markers when encountered.
     */
    private computeDisplayNumbers(pages: QuarkdownPage[]): number[] {
        let nextNumber = 1;
        return pages.map(page => {
            const markers = this.getPageNumberResetMarkers(page);
            if (markers.length > 0) {
                const lastMarker = markers[markers.length - 1];
                const requested = parseInt(lastMarker.dataset.start || '1', 10);
                nextNumber = Number.isFinite(requested) && requested > 0 ? requested : 1;
            }

            const displayNumber = nextNumber;
            nextNumber += 1;
            return displayNumber;
        });
    }

    /**
     * Stores the computed display number on the underlying DOM node for later consumers (e.g. TOC).
     */
    private setDisplayNumberMetadata(page: QuarkdownPage, value: number) {
        const asHTMLElement = page as HTMLElement;
        if (asHTMLElement?.dataset) {
            asHTMLElement.dataset.displayPageNumber = value.toString();
            return;
        }

        const asSlidesPage = page as { slide?: HTMLElement };
        if (asSlidesPage.slide) {
            asSlidesPage.slide.dataset.displayPageNumber = value.toString();
        }
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
     * Updates all current page number elements with their respective (possibly reset) page numbers.
     */
    private updateCurrentPageNumbers(pages: QuarkdownPage[]) {
        const displayNumbers = this.computeDisplayNumbers(pages);
        pages.forEach((page, index) => {
            const number = displayNumbers[index];
            this.setDisplayNumberMetadata(page, number);
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