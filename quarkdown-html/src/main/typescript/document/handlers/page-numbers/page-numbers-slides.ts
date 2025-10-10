import {PageNumbersDocumentHandler} from "./page-numbers-document-handler";

/**
 * Page numbering handler for Reveal.js slide presentations.
 * Updates page counters to reflect the current slide position and total slide count.
 */
export class PageNumbersSlides extends PageNumbersDocumentHandler {
    /**
     * Updates all total page number elements with the total count of slides.
     * Counts all section and div elements directly under .reveal .slides.
     */
    private updateTotalPageNumbers() {
        const slides = document.querySelectorAll('.reveal .slides > :is(section, div)');
        this.getTotalPageNumberElements().forEach(total => {
            total.innerText = slides.length.toString();
        });
    }

    /**
     * Updates all current page number elements with their respective slide indices.
     * Determines the slide index based on the element's position within its container.
     */
    private updateCurrentPageNumbers() {
        this.getCurrentPageNumberElements().forEach(current => {
            // The page counter can be either in a slide or a background.
            const container = current.closest('.reveal > :is(.slides, .backgrounds)');
            const ownSlide = current.closest('.reveal > :is(.slides, .backgrounds) > :is(section, div)');
            if (!container || !ownSlide) return;

            const index = Array.from(container.children).indexOf(ownSlide)
            current.innerText = (index + 1).toString();
        });
    }

    /**
     * Updates both total and current page numbers after rendering completes.
     */
    onPostRendering() {
        this.updateTotalPageNumbers();
        this.updateCurrentPageNumbers();
    }
}