class AsyncExecutionQueue {
    constructor(onExecute) {
        this.queue = [];
        this.onExecute = onExecute;
    }

    push(fn) {
        this.queue.push(fn);
    }

    async execute() {
        await Promise.all(this.queue.map(async fn => fn()));
        this.queue = [];
        if (this.onExecute) this.onExecute();
        this.completed = true;
    }

    isCompleted() {
        return this.completed;
    }
}

// Queue of actions to be executed before the document is handled by Reveal/Paged.
// The document is elaborated only after this queue is executed.
const preRenderingExecutionQueue = new AsyncExecutionQueue();
// Queue of actions to be executed after the document has been rendered in its final form.
const postRenderingExecutionQueue = new AsyncExecutionQueue();

/**
 * Returns whether the document is finalized and ready.
 * This can be watched and waited for by other tools, such as Puppeteer to generate a PDF.
 * @returns {boolean}
 */
function isReady() {
    return preRenderingExecutionQueue.isCompleted() && postRenderingExecutionQueue.isCompleted();
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
     * Handles or transforms footnotes in the post-rendering execution queue.
     * @param {{definition: Element, reference: Element}[]} footnotes footnote definitions and their first reference element.
     */
    handleFootnotes(footnotes) {
    }

    /**
     * @returns {Element} The parent viewport of the given element,
     * such as the slide section in slides or the page area in paged.
     */
    getParentViewport(element) {
        return document.documentElement;
    }

    /**
     * Updates the content of `.current-page-number` and `.total-page-number` elements.
     */
    updatePageNumberElements() {
    }

    /**
     * Populates the execution queue with the necessary functions to be executed after the document is ready.
     */
    populateExecutionQueue() {
        postRenderingExecutionQueue.push(() => this.copyPageMarginInitializers());
        postRenderingExecutionQueue.push(() => this.updatePageNumberElements());
        postRenderingExecutionQueue.push(() => {
            moveFootnoteDefinitionsToEnd(this.getParentViewport);
            this.handleFootnotes(getFootnoteDefinitionsAndFirstReference());
        });
        if (this.usesNavigationSidebar()) {
            postRenderingExecutionQueue.push(createSidebar);
        }
    }

    usesNavigationSidebar() {
        return true;
    }

    /**
     * Sets up a hook called before the document is processed.
     */
    setupBeforeReadyHook() {
        document.addEventListener('DOMContentLoaded', async () => {
            await preRenderingExecutionQueue.execute();
            this.beforeReady(document);
        });
    }

    /**
     * Sets up a hook called after the document is processed.
     */
    setupAfterReadyHook() {
        document.addEventListener('DOMContentLoaded', () => postRenderingExecutionQueue.execute());
    }
}

class PlainDocument extends QuarkdownDocument {
}

let doc = new PlainDocument(); // Overwritten externally by html-wrapper

//
// Footnotes.

/**
 * @returns {{definition: Element, reference: Element}[]} the footnote definitions and their first non-null reference element.
 */
function getFootnoteDefinitionsAndFirstReference() {
    const definitions = document.querySelectorAll('aside.footnote-definition');
    // For each definition, gets the first reference to it.
    // The rendered footnote will be placed in the first reference's page/section.
    return Array.from(definitions).map(definition => {
        const id = definition.id;
        const reference = document.querySelector(`.footnote-reference[data-definition="${id}"]`);
        return reference ? {definition, reference} : null;
    }).filter(item => item !== null);
}

/**
 * Moves footnote definitions to the end of the parent page viewport. This is performed in the post-rendering stage, where pages are already formed.
 * This is needed because Quarkdown may render definitions in the middle of the page, and that would compromise 'following' (`+`) selectors.
 * @param {function} parentElementFunction a function that takes a footnote definition and returns its parent viewport, such as its parent page.
 */
function moveFootnoteDefinitionsToEnd(parentElementFunction) {
    const definitions = document.body.querySelectorAll('aside.footnote-definition');
    definitions.forEach(definition => {
        const parent = parentElementFunction(definition);
        definition.remove();
        parent.appendChild(definition);
    });
}

//
// Enables toggling of the collapsed/expanded state of inline elements.

postRenderingExecutionQueue.push(() => {
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

// For elements marked with .fill-height, the CSS variable --viewport-remaining-height is injected.
function applyRemainingHeightProperties() {
    const fillHeightElements = document.querySelectorAll('.fill-height');

    fillHeightElements.forEach(element => {
        const contentArea = doc.getParentViewport(element)
        if (!contentArea) return;
        const remainingHeight = contentArea.getBoundingClientRect().bottom - element.getBoundingClientRect().top;

        // Inject CSS variable.
        element.style.setProperty('--viewport-remaining-height', `${remainingHeight}px`);
    });
}

postRenderingExecutionQueue.push(applyRemainingHeightProperties);

// General hash utility.
function hashCode(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = ((hash << 5) - hash) + str.charCodeAt(i);
        hash |= 0;
    }
    return hash.toString();
}

//
// Scroll position restoration.

const scrollYStorageKey = "scrollY";
const storedScrollY = +sessionStorage.getItem(scrollYStorageKey);
let scrollRestored = false;

// Saves scroll position.
function saveScrollPosition() {
    history.scrollRestoration = "manual";
    sessionStorage.setItem(scrollYStorageKey, window.scrollY.toString());
}

// Restores scroll position. Even if called multiple times, it will only restore the first time.
function restoreScrollPosition() {
    if (scrollRestored || !storedScrollY) return;

    console.log("Restoring scroll position to", storedScrollY);
    scrollRestored = true;
    requestAnimationFrame(() => {
        window.scrollTo({top: storedScrollY, behavior: "auto"});
    });
}

window.addEventListener("beforeunload", saveScrollPosition);
postRenderingExecutionQueue.push(restoreScrollPosition)

//
// Page chunker.

// Utility that splits content into chunks based on page break elements.
// Example of chunking on a slides document:
// Input:
// <div class="reveal">
//     <div class="slides">
//         <p>First</p>
//         <div class="page-break"></div>
//         <p>Second</p>
//     </div>
// </div>
//
// Output:
// <div class="reveal">
//     <div class="slides">
//         <section>
//             <p>First</p>
//          </section>
//         <section>
//             <p>Second</p>
//         </section>
//     </div>
// </div>
class PageChunker {
    chunks = [];

    constructor(container) {
        this.container = container;
    }

    // Whether an element is not visible in the document.
    isHidden(element) {
        return element.hasAttribute('data-hidden');
    }

    // Whether a chunk has no visible content.
    isBlank(chunk) {
        return chunk.childNodes.length === 0 || Array.from(chunk.children).every(child => this.isHidden(child));
    }

    // Generates chunks based on the page break elements.
    // Page break elements are not preserved in the chunked output.
    generateChunks(createElement) {
        let chunks = [];
        let currentChunk = createElement();

        Array.from(this.container.children).forEach(child => {
            if (child.className === 'page-break') {
                // If we hit a page break, finalize the current section and start a new one.
                chunks.push(currentChunk);
                currentChunk = createElement();
            } else {
                // Otherwise, add the child to the current section.
                currentChunk.appendChild(child);
            }
        });

        // Add the last section if it has any content.
        if (currentChunk.childNodes.length > 0) {
            chunks.push(currentChunk);
        }

        this.chunks = chunks;
    }

    // Applies the generated chunks to the container.
    apply() {
        // Clear out the original slides div and add the new sections.
        this.container.innerHTML = '';
        // Elements that are not part of a section yet and will be added to the next one.
        let queuedElements = [];

        this.chunks.forEach(chunk => {
            // Empty slides are ignored.
            if (this.isBlank(chunk)) {
                // If the section is blank and NOT empty,
                // meaning all its children are hidden (e.g. a marker produced by the .marker function),
                // they are added to the queued elements in order to be added to the next section
                // and not produce an empty slide.
                queuedElements.push(...chunk.children);
            } else {
                // If there are any queued elements, they are added to the beginning of the new section.
                if (queuedElements.length > 0) {
                    queuedElements.forEach(element => chunk.prepend(element));
                    queuedElements = [];
                }
                this.container.appendChild(chunk);
            }
        });

        // If there are any queued elements left, they are added to the last visible section.
        if (queuedElements.length > 0 && this.chunks.length > 0) {
            queuedElements.forEach(element => this.container.lastChild.appendChild(element));
            queuedElements = [];
        }
    }

    chunk() {
        const createElement = () => {
            const element = document.createElement('section');
            element.className = 'chunk';
            return element;
        };
        this.generateChunks(createElement);
        this.apply();
    }
}

//
// Navigation sidebar.

function createSidebar() {
    const sidebar = document.createElement('div');
    sidebar.className = 'sidebar';
    sidebar.style.position = 'fixed';

    const sidebarList = document.createElement('ol');

    let currentActiveListItem = null;

    document.querySelectorAll('h1, h2, h3').forEach(header => {
        const listItem = document.createElement('li');
        listItem.className = header.tagName.toLowerCase();
        listItem.innerHTML = `<a href="#${header.id}"><span>${header.textContent}</span></a>`;
        sidebarList.appendChild(listItem);

        function checkForActive() {
            const rect = header.getBoundingClientRect();
            if (rect.top <= window.innerHeight * 0.5 && rect.top + rect.height >= 0) {
                currentActiveListItem?.classList.remove('active');
                currentActiveListItem = listItem;
                currentActiveListItem.classList.add('active');
            }
        }

        checkForActive()
        window.addEventListener('scroll', checkForActive);
    });

    sidebar.appendChild(sidebarList);
    document.body.appendChild(sidebar);
    return sidebar;
}