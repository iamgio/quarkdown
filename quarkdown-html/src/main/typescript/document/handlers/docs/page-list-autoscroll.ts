import {DocumentHandler} from "../../document-handler";

const PAGE_LIST_SELECTOR = 'nav[data-role="page-list"]';
const CURRENT_PAGE_SELECTOR = "[aria-current]";

/**
 * Document handler that scrolls the page list sidebar to show the current page.
 * Finds the first element with `[aria-current]` in the page list and scrolls
 * its containing aside to bring it into view.
 */
export class PageListAutoscroll extends DocumentHandler {
    async onPostRendering() {
        const pageList = document.querySelector(PAGE_LIST_SELECTOR);
        if (!pageList) return;

        const currentPage = pageList.querySelector(CURRENT_PAGE_SELECTOR);
        if (!currentPage) return;

        const aside = currentPage.closest("aside");
        if (!aside) return;

        const currentPageElement = currentPage as HTMLElement;
        const asideRect = aside.getBoundingClientRect();
        const currentRect = currentPageElement.getBoundingClientRect();

        const scrollTop = currentRect.top - asideRect.top + aside.scrollTop - aside.clientHeight / 4;
        aside.scrollTop = Math.max(0, scrollTop);
    }
}
