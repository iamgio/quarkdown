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

            const footnoteAreaParent = page.classList.contains('pdf-page')
                ? page.querySelector('section')!
                : page;

            definition.remove();
            getOrCreateFootnoteArea(footnoteAreaParent)?.appendChild(definition);
        });
    }
}