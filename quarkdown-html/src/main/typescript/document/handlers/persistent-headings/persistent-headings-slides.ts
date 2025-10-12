import {PersistentHeadingsDocumentHandler} from "./persistent-headings-document-handler";

/**
 * Handles persistent headings for slide presentations.
 */
export class PersistentHeadingsSlides extends PersistentHeadingsDocumentHandler {
    /**
     * Post-rendering hook that applies persistent headings to both the slide and background elements.
     */
    async onPostRendering() {
        const slides = document.querySelectorAll('.reveal .slides > :is(section, .pdf-page)');
        const backgrounds = document.querySelectorAll('.reveal > :is(.backgrounds, .pdf-page) > .slide-background');

        slides.forEach((slide, index) => {
            this.apply({
                sourceContainer: slide,
                targetContainers: [slide, backgrounds[index]].filter(Boolean),
            });
        });
    }
}