import {DocumentHandler} from "../../document-handler";

/**
 */
export abstract class PageMarginsDocumentHandler extends DocumentHandler {
    protected pageMarginInitializers: HTMLElement[] = [];

    onPreRendering() {
        this.pageMarginInitializers = Array.from(document.querySelectorAll('.page-margin-content'));

        // Removing them from the document to prevent display before rendering.
        this.pageMarginInitializers.forEach(initializer => initializer.remove());
    }

    /**
     * Called after the main rendering process is complete,
     * this function is responsible for injecting page margin content
     * into the document at appropriate locations on each page.
     */
    abstract onPostRendering(): void;
}