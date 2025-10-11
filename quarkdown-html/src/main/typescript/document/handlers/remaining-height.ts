import {DocumentHandler} from "../document-handler";

/**
 * Handler that calculates the remaining height in the viewport for elements
 * with the `fill-height` class, and sets a CSS variable `--viewport-remaining-height`
 * on those elements. This allows such elements to adapt their height based on
 * the available space in the viewport.
 */
export class RemainingHeight extends DocumentHandler {
    onPostRendering() {
        const fillHeightElements = document.querySelectorAll<HTMLElement>('.fill-height');

        fillHeightElements.forEach(element => {
            const contentArea = this.quarkdownDocument.getParentViewport(element)
            if (!contentArea) return;
            const remainingHeight = contentArea.getBoundingClientRect().bottom - element.getBoundingClientRect().top;

            // Inject CSS variable.
            element.style.setProperty('--viewport-remaining-height', `${remainingHeight}px`);
        });
    }
}