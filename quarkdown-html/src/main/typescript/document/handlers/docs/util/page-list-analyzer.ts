const PAGE_LIST_NAV_SELECTOR = 'nav[data-role="page-list"]';
const CURRENT_PAGE_SELECTOR = 'a[aria-current="page"]';

/**
 * Analyzes a page list navigation element to find previous and next page links.
 * The page list is a possibly nested ordered list where the current page is marked
 * with `aria-current="page"`.
 */
export class PageListAnalyzer {
    private readonly nav: HTMLElement | null;

    constructor() {
        this.nav = document.querySelector<HTMLElement>(PAGE_LIST_NAV_SELECTOR);
    }

    /**
     * Gets all anchor elements within the page list in document order.
     */
    private getAllLinks(): HTMLAnchorElement[] {
        if (!this.nav) return [];
        return Array.from(this.nav.querySelectorAll<HTMLAnchorElement>("a"));
    }

    /**
     * Finds the index of the current page link within all links.
     */
    private getCurrentIndex(links: HTMLAnchorElement[]): number {
        return links.findIndex((link) => link.matches(CURRENT_PAGE_SELECTOR));
    }

    /**
     * Returns the previous page link, or null if there is no previous page.
     */
    getNextPageLink(): HTMLAnchorElement | null {
        const links = this.getAllLinks();
        const currentIndex = this.getCurrentIndex(links);
        if (currentIndex === -1 || currentIndex >= links.length - 1) return null;
        return links[currentIndex + 1];
    }

    /**
     * Returns the previous page link, or null if there is no previous page.
     */
    getPreviousPageLink(): HTMLAnchorElement | null {
        const links = this.getAllLinks();
        const currentIndex = this.getCurrentIndex(links);
        if (currentIndex <= 0) return null;
        return links[currentIndex - 1];
    }
}
