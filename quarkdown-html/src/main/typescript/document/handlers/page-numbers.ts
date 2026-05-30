import {DocumentHandler} from "../document-handler";
import {PagedLikeQuarkdownDocument, QuarkdownPage} from "../paged-like-quarkdown-document";
import {formatNumber} from "../../util/numbering";

/**
 * Abstract base class for document handlers that manage page numbering.
 * Provides utility methods to find and update page number elements in documents,
 * including support for page number resets and displaying page numbers in tables of contents.
 */
export class PageNumbers extends DocumentHandler<PagedLikeQuarkdownDocument<any>> {
    private static readonly TOC_NAV_SELECTOR = 'nav[data-role="table-of-contents"]';
    private static readonly GENERATED_TOC_ID_PREFIX = 'qd-location-';

    /**
     * Escapes a string so it can be safely used in a CSS attribute selector.
     */
    private escapeForAttributeSelector(value: string): string {
        return value.replace(/\\/g, '\\\\').replace(/"/g, '\\"');
    }

    /**
     * Converts a location label like "2.1" into a stable id-friendly token.
     */
    private toLocationToken(location: string): string {
        return location.toLowerCase().replace(/[^a-z0-9_-]+/g, '-').replace(/^-+|-+$/g, '');
    }

    /**
     * Finds a unique available ID based on a preferred candidate.
     */
    private getAvailableId(preferred: string): string {
        if (!document.getElementById(preferred)) {
            return preferred;
        }

        let suffix = 2;
        while (document.getElementById(`${preferred}-${suffix}`)) {
            suffix += 1;
        }
        return `${preferred}-${suffix}`;
    }

    /**
     * Ensures TOC anchors point to an unambiguous target id, even when duplicate heading ids exist.
     */
    private updateTableOfContentsHref(anchor: HTMLAnchorElement, target: HTMLElement, location: string) {
        const hasUsableId = target.id.length > 0 && document.getElementById(target.id) === target;
        if (!hasUsableId) {
            const locationToken = this.toLocationToken(location) || 'item';
            const generatedId = this.getAvailableId(`${PageNumbers.GENERATED_TOC_ID_PREFIX}${locationToken}`);
            target.id = generatedId;
        }
        anchor.setAttribute('href', `#${target.id}`);
    }

    /**
     * Resolves a TOC anchor target from its list item `data-location`, falling back to href id lookup.
     */
    private resolveTableOfContentsTarget(anchor: HTMLAnchorElement): HTMLElement | undefined {
        const location = anchor.closest<HTMLElement>('li[data-location]')?.dataset.location;
        if (location) {
            const escaped = this.escapeForAttributeSelector(location);
            const candidates = document.querySelectorAll<HTMLElement>(`[data-location="${escaped}"]`);
            const target = Array.from(candidates).find(element => !element.closest(PageNumbers.TOC_NAV_SELECTOR));
            if (target) {
                this.updateTableOfContentsHref(anchor, target, location);
                return target;
            }
        }

        const href = anchor.getAttribute('href');
        if (!href || !href.startsWith('#')) {
            return undefined;
        }

        const id = decodeURIComponent(href).slice(1);
        return id ? document.getElementById(id) ?? undefined : undefined;
    }

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
        const tocs = document.querySelectorAll<HTMLElement>(PageNumbers.TOC_NAV_SELECTOR);
        tocs.forEach(nav => {
            nav.querySelectorAll<HTMLAnchorElement>(':scope a[href^="#"]').forEach(anchor => {
                const target = this.resolveTableOfContentsTarget(anchor);
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