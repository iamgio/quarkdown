import {PageMarginsDocumentHandler} from "./page-margins-document-handler";

export class PageMarginsSlides extends PageMarginsDocumentHandler {
    /**
     * Copy the content of each global page margin initializer to the background of each section.
     *
     * @example
     * ```html
     * <div class="page-margin-content-initializer page-margin-bottom-center">Hello</div>
     * ```
     *
     * will be copied to section as:
     *
     * ```html
     * <div class="page-margin-content page-margin-bottom-center">Hello</div>
     * ```
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