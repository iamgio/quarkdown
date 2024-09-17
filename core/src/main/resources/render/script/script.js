// Actions to be executed after the document has been loaded.
let executionQueue = [];

function executeQueue() {
    executionQueue.forEach((fn) => fn());
}

const PLAIN = "plain";
const SLIDES = "slides";
const PAGED = "paged";

let docType = PLAIN; // Overridden externally by html-wrapper

// Whether the document type is plain.
function isPlain() {
    return docType === PLAIN;
}

// Whether the document type is slides.
function isSlides() {
    return docType === SLIDES;
}

// Whether the document type is paged.
function isPaged() {
    return docType === PAGED;
}

// Sets up the execution of the queue based on the document type.
function setupQueueExecution() {
    if (isPaged()) {
        setupPagedHandler();
        return;
    }

    document.addEventListener('DOMContentLoaded', executeQueue);
}

// Enables toggling of the collapsed/expanded state of inline elements.

executionQueue.push(() => {
    // Add click event listener to the collapsible spans.
    const collapsibles = document.querySelectorAll('.inline-collapse');
    collapsibles.forEach((span) => {
        span.addEventListener('click', toggleCollapse);
    });
});

// Toggles the collapsed/expanded state of inline collapsibles.
function toggleCollapse(event) {
    const span = event.target;
    const fullText = span.dataset.fullText;
    const collapsedText = span.dataset.collapsedText;
    const collapsed = span.dataset.collapsed === 'true';

    // Toggle between the full and collapsed text.
    span.textContent = collapsed ? fullText : collapsedText;
    span.dataset.collapsed = (!collapsed).toString();
}