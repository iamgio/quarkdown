import {DocumentHandler} from "../../document-handler";
import {isSafari} from "../../../util/browser";

const PAGE_LIST_SELECTOR = 'nav[data-role="page-list"]';
const CURRENT_PAGE_SELECTOR = "[aria-current]";
const STORAGE_KEY = "qd-page-list-scroll";

/**
 * Document handler that scrolls the page list sidebar to show the current page.
 * Restores the previous scroll position from sessionStorage, then smooth scrolls
 * to bring the current page into view.
 *
 * On Safari, only the scroll position is restored without further adjustments,
 * as Safari's smooth scroll does not respect a programmatically set starting position.
 */
export class PageListAutoscroll extends DocumentHandler {
    async onPostRendering() {
        const pageList = document.querySelector(PAGE_LIST_SELECTOR);
        if (!pageList) return;

        const currentPage = pageList.querySelector(CURRENT_PAGE_SELECTOR) as HTMLElement | null;
        if (!currentPage) return;

        const aside = currentPage.closest("aside") as HTMLElement | null;
        if (!aside) return;

        this.restoreScrollPosition(aside);

        if (!isSafari()) {
            this.scrollToCurrentPage(aside, currentPage);
        }

        this.saveScrollPositionOnScroll(aside);
    }

    private restoreScrollPosition(aside: HTMLElement) {
        const savedScrollTop = sessionStorage.getItem(STORAGE_KEY);
        if (savedScrollTop !== null) {
            aside.scrollTop = parseFloat(savedScrollTop);
        }
    }

    private scrollToCurrentPage(aside: HTMLElement, currentPage: HTMLElement) {
        const asideRect = aside.getBoundingClientRect();
        const currentRect = currentPage.getBoundingClientRect();
        const targetScrollTop = Math.max(0, currentRect.top - asideRect.top + aside.scrollTop - aside.clientHeight / 4);

        aside.scrollTo({
            top: targetScrollTop,
            behavior: "smooth",
        });
    }

    private saveScrollPositionOnScroll(aside: HTMLElement) {
        aside.addEventListener("scroll", () => {
            sessionStorage.setItem(STORAGE_KEY, aside.scrollTop.toString());
        });
    }
}
