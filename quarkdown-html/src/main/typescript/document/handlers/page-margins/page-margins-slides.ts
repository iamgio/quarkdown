import {PageMarginsDocumentHandler} from "./page-margins-document-handler";

/**
 * Page margins handler for slides documents, which copies page margin content to all slide backgrounds.
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
 * <div class="page-margin-content page-margin-bottom-center">Hello</div>
 * ```
 */
export class PageMarginsSlides extends PageMarginsDocumentHandler {
    /**
     * Copies all page margin initializers to every slide background.
     * Each initializer is cloned and appended to all slide backgrounds.
     */
    onPostRendering() {
        this.pageMarginInitializers.forEach(initializer => {
            const pageMargin = document.createElement('div');
            pageMargin.className = initializer.className;
            pageMargin.innerHTML = initializer.innerHTML;

            // Append the page margin to all slide backgrounds.
            const slideBackgrounds = document.querySelectorAll('.slide-background');
            slideBackgrounds.forEach(slideBackground => {
                slideBackground.appendChild(pageMargin.cloneNode(true));
            });
        });
    }
}