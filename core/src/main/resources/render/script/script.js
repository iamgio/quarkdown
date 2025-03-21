//
// Actions to be executed after the document has been loaded.

let executionQueue = [];

// Global state.
let readyState = false;

/**
 * Returns whether the document is finalized and ready.
 * This can be watched by other tools (e.g. Puppeteer).
 * @returns {boolean}
 */
function isReady() {
    return readyState;
}

function executeQueue() {
    executionQueue.forEach((fn) => fn());
    readyState = true;
}

//
// Different kinds of documents.

/**
 * A Quarkdown document, inherited by each specific document type.
 */
class QuarkdownDocument {
    pageMarginInitializers;

    /**
     * Prepares the stages of the document runtime.
     */
    prepare() {
        this.populateExecutionQueue();
        this.setupBeforeReadyHook();
        this.setupAfterReadyHook();
    }

    /**
     * Hook to be executed before the document is ready.
     * For instance, this is run before paged.js or Reveal.js processes the content.
     * @param content pre-processed content
     */
    beforeReady(content) {
        // A page margin content initializer is an element that will be copied into each section,
        // and is placed on one of the page margins.
        this.pageMarginInitializers = content.querySelectorAll('.page-margin-content');
    }

    /**
     * Removes all page margin initializers from the document.
     */
    removeAllPageMarginInitializers() {
        this.pageMarginInitializers.forEach(initializer => initializer.remove());
    }

    /**
     * To be run after the document is ready.
     * This pastes the previously-loaded page margin initializers into each new processed page.
     */
    copyPageMarginInitializers() {
        if (!this.pageMarginInitializers) {
            console.error('pageMarginInitializers not set');
        }
    }

    /**
     * Updates the content of `.current-page-number` and `.total-page-number` elements.
     */
    updatePageNumberElements() {}

    /**
     * Populates the execution queue with the necessary functions to be executed after the document is ready.
     */
    populateExecutionQueue() {
        executionQueue.push(
            () => this.copyPageMarginInitializers(),
            () => this.updatePageNumberElements()
        );
    }

    /**
     * Sets up a hook called before the document is processed.
     */
    setupBeforeReadyHook() {
        document.addEventListener('DOMContentLoaded', () => this.beforeReady(document));
    }

    /**
     * Sets up a hook called after the document is processed.
     */
    setupAfterReadyHook() {
        document.addEventListener('DOMContentLoaded', executeQueue);
    }
}

class PlainDocument extends QuarkdownDocument {}

let doc = new PlainDocument(); // Overridden externally by html-wrapper

//
// Enables toggling of the collapsed/expanded state of inline elements.

executionQueue.push(() => {
    // Add click event listener to the collapsible spans.
    const collapsibles = document.querySelectorAll('.inline-collapse');
    collapsibles.forEach((span) => {
        span.addEventListener('click', () => toggleCollapse(span));
    });
});

function toggleCollapse(span) {
    const fullText = span.dataset.fullText;
    const collapsedText = span.dataset.collapsedText;
    const collapsed = span.dataset.collapsed === 'true';

    // Toggle between the full and collapsed text.
    const content = collapsed ? fullText : collapsedText;

    span.dataset.collapsed = (!collapsed).toString();

    const isUserDefined = span.closest('.error') === null;
    if (isUserDefined) {
        span.innerHTML = content;
    } else {
        span.textContent = content;
    }
}