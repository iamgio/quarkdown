import {PageMarginsDocumentHandler} from "./page-margins-document-handler";
import {SlidesPage} from "../../type/slides-document";

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
     * Copies all page margin initializers to the slide background.
     */
    apply(initializer: HTMLElement, page: SlidesPage, marginPositionName: string) {
        // Append the page margin to all slide backgrounds.
        const pageMargin = document.createElement('div');
        this.pushMarginClassList(pageMargin, initializer, marginPositionName);
        pageMargin.innerHTML = initializer.innerHTML;

        page.background.appendChild(pageMargin);
    }
}