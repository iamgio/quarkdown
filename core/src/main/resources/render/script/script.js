// Different kinds of documents.

class QuarkdownDocument {
    setupQueueExecutionHook() {
        document.addEventListener('DOMContentLoaded', executeQueue);
    }
}

class PlainDocument extends QuarkdownDocument {}

let doc = new PlainDocument(); // Overridden externally by html-wrapper

//
// Actions to be executed after the document has been loaded.

let executionQueue = [];

function executeQueue() {
    executionQueue.forEach((fn) => fn());
}

//
// Enables toggling of the collapsed/expanded state of inline elements.

executionQueue.push(() => {
    // Add click event listener to the collapsible spans.
    const collapsibles = document.querySelectorAll('.inline-collapse');
    collapsibles.forEach((span) => {
        span.addEventListener('click', () => toggleCollapse(span));
    });
});

//
// Toggles the collapsed/expanded state of inline collapsibles.

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