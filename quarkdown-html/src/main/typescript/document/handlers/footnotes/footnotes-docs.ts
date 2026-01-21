import {FootnotesDocumentHandler} from "./footnotes-document-handler";

/**
 * Footnote handler for docs documents that renders footnotes at the bottom of the page.
 */
export class FootnotesDocs extends FootnotesDocumentHandler {
    async onPostRendering() {
        const footnoteArea = document.getElementById('footnote-area');
        if (!footnoteArea) return;

        if (this.footnotes.length === 0) {
            footnoteArea.remove();
            return;
        }

        this.footnotes.forEach(({definition}) => {
            definition.remove();
            footnoteArea.appendChild(definition);
        });
    }
}