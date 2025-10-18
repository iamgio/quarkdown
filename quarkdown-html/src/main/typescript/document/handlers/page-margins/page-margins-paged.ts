import {PageMarginsDocumentHandler} from "./page-margins-document-handler";

/**
 * Page margins handler for paged documents, which copies page margin content to each page.
 *
 * @example
 * A page margin initializer like:
 * ```html
 * <div class="page-margin-content-initializer page-margin-bottom-center"
 *      data-on-left-page="bottom-center" data-on-right-page="bottom-center">Hello</div>
 * ```
 *
 * will be copied to each section background as:
 *
 * ```html
 * <div class="pagedjs_margin-content">Hello</div>
 * ```
 *
 * contained in the `.pagedjs_margin.pagedjs_margin-bottom-center` div.
 *
 * In case of `outside`/`inside` positions, the `data-on-left-page` and `data-on-right-page` may differ.
 */
export class PageMarginsPaged extends PageMarginsDocumentHandler {
    /**
     * Copies all page margin initializers to every page, in the margin areas.
     */
    async onPostRendering() {
        this.pageMarginInitializers.forEach(initializer => {
            const marginContent = document.createElement("div");
            marginContent.className = "pagedjs_margin-content";
            marginContent.innerHTML = initializer.innerHTML;

            const pages = document.querySelectorAll<HTMLElement>(".pagedjs_page");
            pages.forEach(page => this.apply(initializer, page));
        });
    }

    /**
     * Gets the margin position name for the given page margin initializer, depending on whether the page is left or right.
     * @param initializer The page margin initializer element
     * @param page The page the margin will be applied to
     */
    private getMarginPositionName(initializer: HTMLElement, page: HTMLElement): string | null {
        const isRightPage = page.classList.contains("pagedjs_right_page");
        const pageType = isRightPage ? "right" : "left";
        return initializer.getAttribute(`data-on-${pageType}-page`);
    }

    /**
     * Applies the page margin initializer to the given page.
     * @param initializer The page margin initializer element
     * @param page The page to apply the margin to
     */
    private apply(initializer: HTMLElement, page: HTMLElement) {
        const marginPositionName = this.getMarginPositionName(initializer, page);
        if (!marginPositionName) return;

        // Given the initializer with class "page-margin-content-initializer page-margin-bottom-center",
        // the margin class will be "pagedjs_margin-bottom-center".
        const pageMargins = page.querySelectorAll(`.pagedjs_margin-${marginPositionName}`);
        pageMargins.forEach(pageMargin => {
            pageMargin.classList.add("hasContent"); // Required by paged.js to show the content.

            // Find the container where the content should go.
            const container = pageMargin.querySelector(".pagedjs_margin-content");
            if (!container) return;

            // Copy the classes, allows for styling.
            container.classList.add(
                `page-margin-${marginPositionName}`, // In case of mirror positions (ouside/inside), sets the actual position.
                ...initializer.classList
            );
            // Copy the content of the initializer to each page.
            container.innerHTML = initializer.innerHTML;
        });
    }
}