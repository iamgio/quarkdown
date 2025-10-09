/**
 * Interface for footnote definition and reference pairs.
 */
export interface FootnoteDefinitionAndReference {
    definition: Element;
    reference: Element;
}

/**
 * A Quarkdown document, inherited by each specific document type.
 *
 * This class manages the document lifecycle, including pre-rendering and post-rendering
 * execution queues, page margin content, persistent headings, footnotes, and navigation.
 */
export interface QuarkdownDocument {
    /**
     * Prepares the stages of the document runtime.
     *
     * This method sets up the execution queues and hooks for before/after document processing.
     */
    prepare(): void

    /**
     * Hook to be executed before the document is ready.
     * For instance, this is run before paged.js or Reveal.js processes the content.
     *
     * A page margin content initializer is an element that will be copied into each section,
     * and is placed on one of the page margins.
     *
     * @param content - The pre-processed document content
     */
    beforeReady(content: Document | Element): void

    /**
     * Removes all page margin initializers from the document.
     */
    removeAllPageMarginInitializers(): void

    /**
     * To be run after the document is ready.
     * This copies the previously-loaded page margin initializers into each new processed page.
     */
    copyPageMarginInitializers(): void

    /**
     * To be run after the document is ready.
     * This takes `.last-heading` elements and, according to their `data-depth` attribute,
     * injects the content of the target element into each page.
     * The target element is the last element matching the selector, prior to the `.last-heading` element.
     */
    copyPersistentHeadings(): void

    /**
     * Handles or transforms footnotes in the post-rendering execution queue.
     *
     * @param footnotes - Array of footnote definitions and their first reference element
     */
    handleFootnotes(footnotes: FootnoteDefinitionAndReference[]): void

    /**
     * Returns the parent viewport of the given element,
     * such as the slide section in slides or the page area in paged documents.
     *
     * @param element - The element to find the parent viewport for
     * @returns The parent viewport element
     */
    getParentViewport(element: Element): Element

    /**
     * Updates the content of `.current-page-number` and `.total-page-number` elements.
     */
    updatePageNumberElements(): void

    /**
     * Populates the execution queue with the necessary functions to be executed after the document is ready.
     *
     * This method requires external dependencies like `postRenderingExecutionQueue`,
     * `getFootnoteDefinitionsAndFirstReference()`, and `createSidebar()` to be available.
     */
    populateExecutionQueue(): void

    /**
     * Determines whether this document type uses a navigation sidebar.
     *
     * @returns true if the document should include a navigation sidebar
     */
    usesNavigationSidebar(): boolean

    /**
     * Sets up a hook called before the document is processed.
     *
     * This method requires external dependencies like `preRenderingExecutionQueue`
     * to be available.
     */
    setupBeforeReadyHook(): void

    /**
     * Sets up a hook called after the document is processed.
     *
     * This method requires external dependencies like `postRenderingExecutionQueue`
     * to be available.
     */
    setupAfterReadyHook(): void
}
