const PAGE_LIST_NAV_SELECTOR = 'nav[data-role="page-list"]';
const CURRENT_PAGE_SELECTOR = 'a[aria-current="page"]';

/**
 * Analyzes a page list navigation element to find previous and next page links.
 * The page list is a possibly nested ordered list where the current page is marked
 * with `aria-current="page"`.
 */
export class PageListAnalyzer {
    private readonly nav: HTMLElement | null;
    private readonly currentPageAnchor: HTMLAnchorElement | null;

    constructor() {
        this.nav = document.querySelector<HTMLElement>(PAGE_LIST_NAV_SELECTOR);
        this.currentPageAnchor = this.nav?.querySelector<HTMLAnchorElement>(CURRENT_PAGE_SELECTOR) ?? null;
    }

    /**
     * Gets all anchor elements within the page list in document order,
     * excluding anchor links within the current page.
     */
    private getAllLinks(): HTMLAnchorElement[] {
        if (!this.nav) return [];
        return Array.from(this.nav.querySelectorAll<HTMLAnchorElement>("a"))
            .filter((link) => !this.isSamePageAnchor(link));
    }

    /**
     * Checks if a link is an anchor within the current page.
     */
    private isSamePageAnchor(link: HTMLAnchorElement): boolean {
        const href = link.getAttribute("href");
        if (!href || href.startsWith("#")) return true;
        if (link.pathname === location.pathname && link.hash !== "") return true;
        const currentPageHref = this.currentPageAnchor?.getAttribute("href");
        return !!(currentPageHref && href.startsWith(currentPageHref + "#"));
    }

    /**
     * Finds the index of the current page link within all links.
     */
    private getCurrentIndex(links: HTMLAnchorElement[]): number | null {
        if (!this.currentPageAnchor) return null;
        const index = links.indexOf(this.currentPageAnchor);
        return index === -1 ? null : index;
    }

    /**
     * Returns the next page link, or null if there is no next page.
     */
    getNextPageLink(): HTMLAnchorElement | null {
        const links = this.getAllLinks();
        const currentIndex = this.getCurrentIndex(links);
        if (currentIndex === null || currentIndex >= links.length - 1) return null;
        return links[currentIndex + 1];
    }

    /**
     * Returns the previous page link, or null if there is no previous page.
     */
    getPreviousPageLink(): HTMLAnchorElement | null {
        const links = this.getAllLinks();
        const currentIndex = this.getCurrentIndex(links);
        if (currentIndex === null || currentIndex === 0) return null;
        return links[currentIndex - 1];
    }
}
