import {DocumentHandler} from "../../document-handler";

/**
 * Abstract base class for document handlers that manage page margin content.
 * Collects page margin initializers during pre-rendering and positions them appropriately
 * on each page in the final document based on the document type.
 */
export abstract class PageMarginsDocumentHandler extends DocumentHandler {
    /** Array of page margin initializer elements collected from the document */
    protected pageMarginInitializers: HTMLElement[] = [];

    /**
     * Collects all page margin content initializers and removes them from the document.
     * This prevents them from being displayed before proper positioning.
     */
    onPreRendering() {
        this.pageMarginInitializers = Array.from(document.querySelectorAll('.page-margin-content'));
        this.pageMarginInitializers.forEach(initializer => initializer.remove());
    }

    /**
     * Called after the main rendering process is complete,
     * this function is responsible for injecting page margin content
     * into the document at appropriate locations on each page.
     */
    abstract onPostRendering(): void;
}