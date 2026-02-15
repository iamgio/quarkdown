import {DocumentHandler} from "../document-handler";
import {PagedLikeQuarkdownDocument, QuarkdownPage} from "../paged-like-quarkdown-document";
import {getAnchorTargetId} from "../../util/id";
import {formatNumber} from "../../util/numbering";

/**
 * Abstract base class for document handlers that manage page numbering.
 * Provides utility methods to find and update page number elements in documents,
 * including support for page number resets and displaying page numbers in tables of contents.
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
     * Finds all page number format markers contained in the given page.
     */
    private getPageNumberFormatMarkers(page: QuarkdownPage): HTMLElement[] {
        return Array.from(page.querySelectorAll('.page-number-formatter'));
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
        let pageNumber = 1;
        let currentFormat = "1";
        pages.forEach(page => {
            // Checking for format markers on the current page. The last format marker on the page determines the format for the page number.
            const formatMarkers = this.getPageNumberFormatMarkers(page);
            formatMarkers.forEach(marker => {
                const format = marker.dataset.format;
                if (format !== undefined) {
                    currentFormat = format;
                }
            });

            // Checking for reset markers on the current page. In that case, the page number is directly updated.
            const resetMarkers = this.getPageNumberResetMarkers(page);
            resetMarkers.forEach(marker => {
                const requested = parseInt(marker.dataset.start || '1', 10);
                if (Number.isFinite(requested) && requested > 0) {
                    pageNumber = requested;
                }
            });

            const formattedPageNumber = formatNumber(pageNumber, currentFormat);
            this.quarkdownDocument.setDisplayPageNumber(page, formattedPageNumber);

            // Applying the page number within the page.
            this.getCurrentPageNumberElements(page).forEach(pageNumberElement => {
                pageNumberElement.innerText = formattedPageNumber;
            });

            pageNumber += 1;
        });
    }

    /**
     * Updates table of contents entries so they display the logical (reset-aware) page numbers.
     */
    private updateTableOfContentsPageNumbers() {
        const tocs = document.querySelectorAll<HTMLElement>('nav[data-role="table-of-contents"]');
        tocs.forEach(nav => {
            nav.querySelectorAll<HTMLAnchorElement>(':scope a[href^="#"]').forEach(anchor => {
                const targetId = getAnchorTargetId(anchor);
                const target = targetId ? document.getElementById(targetId) : undefined;
                const displayNumber = target ? this.quarkdownDocument.getDisplayPageNumber(this.quarkdownDocument.getPage(target)) : undefined;
                this.setTableOfContentsPageNumber(anchor, displayNumber?.toString());
            });
        });
    }

    /**
     * Sets or updates the page number badge within a table of contents entry.
     * @param anchor - The anchor element representing the TOC entry
     * @param value - The page number to set (if undefined, the badge will be created but left empty)
     */
    private setTableOfContentsPageNumber(anchor: HTMLAnchorElement, value?: string) {
        let badge = anchor.querySelector<HTMLElement>('.toc-page-number');
        if (!badge) {
            badge = document.createElement('span');
            badge.className = 'toc-page-number';
            anchor.appendChild(badge);
        }
        if (value) {
            badge.innerText = value;
        }
    }

    /**
     * Updates both total and current page numbers after rendering completes.
     */
    async onPostRendering() {
        const pages = this.quarkdownDocument.getPages();
        this.updateTotalPageNumbers(pages);
        this.updateCurrentPageNumbers(pages);
        this.updateTableOfContentsPageNumbers();
    }
}