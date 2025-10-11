import {getOrCreateFootnoteArea} from "../../../footnotes/footnote-dom";
import {FootnotesDocumentHandler} from "./footnotes-document-handler";

/**
 * Footnote handler for slides documents that renders footnotes at the bottom of the slide.
 */
export class FootnotesSlides extends FootnotesDocumentHandler {
    async onPostRendering() {
        this.footnotes.forEach(({reference, definition}) => {
            const page = this.quarkdownDocument.getParentViewport(reference);
            if (!page) return;

            definition.remove();
            getOrCreateFootnoteArea(page)?.appendChild(definition);
        });
    }
}