import * as plain from "../../type/plain-document";
import {FootnotesDocumentHandler} from "./footnotes-document-handler";

/**
 * Footnote handler for plain documents that renders footnotes in the right margin area.
 * Positions footnotes vertically aligned with their references.
 */
export class FootnotesPlain extends FootnotesDocumentHandler {
    /** Sets up listener to re-render footnotes on resize. */
    init() {
        window.addEventListener('resize', () => this.onPostRendering?.());
    }

    /**
     * Calculates the bottom offset of the last definition in the margin area.
     * @param marginArea - The margin area containing footnote definitions
     * @returns The bottom offset in pixels, or the top of the margin area if empty
     */
    private getLastDefinitionOffset(marginArea: HTMLElement): number {
        const lastChild = marginArea.lastElementChild;
        return lastChild
            ? lastChild.getBoundingClientRect().bottom
            : marginArea.getBoundingClientRect().top;
    }

    /**
     * Renders footnotes in the right margin area, positioned to align with their references.
     * Removes footnotes from their original locations and repositions them in the margin.
     */
    onPostRendering() {
        const rightMarginArea = plain.getRightMarginArea();
        if (!rightMarginArea) return;
        rightMarginArea.innerHTML = '';

        this.footnotes.forEach(({definition, reference}) => {
            definition.remove();
            definition.style.marginTop =
                Math.max(
                    0,
                    reference.getBoundingClientRect().top - this.getLastDefinitionOffset(rightMarginArea)
                ) + 'px';

            rightMarginArea.appendChild(definition);
        });
    }
}