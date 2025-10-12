import {PageMarginsDocumentHandler} from "./page-margins-document-handler";

/**
 * Page margins handler for paged documents, which copies page margin content to each page.
 *
 * @example
 * A page margin initializer like:
 * ```html
 * <div class="page-margin-content-initializer page-margin-bottom-center">Hello</div>
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
    /**
     * Copies all page margin initializers to every page, in the margin areas.
     */
    async onPostRendering() {
        this.pageMarginInitializers.forEach(initializer => {
            const marginContent = document.createElement('div');
            marginContent.className = "pagedjs_margin-content";
            marginContent.innerHTML = initializer.innerHTML;

            // Given the initializer with class "page-margin-content-initializer page-margin-bottom-center",
            // the margin class will be "pagedjs_margin-bottom-center".
            const pageMargins = document.querySelectorAll('.pagedjs_margin-' + initializer.className.split('page-margin-').pop());
            pageMargins.forEach(pageMargin => {
                pageMargin.classList.add("hasContent"); // Required by paged.js to show the content.

                // Append the content.
                const container = pageMargin.querySelector('.pagedjs_margin-content');
                if (!container) return;
                container.classList.add(...initializer.classList); // Copy the classes, allows for styling.
                container.innerHTML = initializer.innerHTML; // Copy the content of the initializer to each page.
            });
        });
    }
}