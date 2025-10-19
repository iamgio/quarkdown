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
 */
export class PageMarginsPaged extends PageMarginsDocumentHandler {
    apply(initializer: HTMLElement, page: HTMLElement, marginPositionName: string): void {
        // Given the initializer with class "page-margin-content-initializer page-margin-bottom-center",
        // the margin class will be "pagedjs_margin-bottom-center".
        const pageMargins = page.querySelectorAll(`.pagedjs_margin-${marginPositionName}`);
        pageMargins.forEach(pageMargin => {
            pageMargin.classList.add("hasContent"); // Required by paged.js to show the content.

            // Find the container where the content should go.
            const container = pageMargin.querySelector<HTMLElement>(".pagedjs_margin-content");
            if (!container) return;

            // Copy the classes, allows for styling.
            this.pushMarginClassList(container, initializer, marginPositionName);
            // Copy the content of the initializer to each page.
            container.innerHTML = initializer.innerHTML;
        });
    }
}