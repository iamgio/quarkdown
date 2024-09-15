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

// Makes error messages collapse their content placed between parentheses, making it expandable when clicked.

executionQueue.push(() => {
    const errors = document.querySelectorAll('.error-box .box-content code');

    errors.forEach((error) => {
        // Extract the content of the error message.
        const text = error.textContent;

        // Regular expression to find the text between parentheses.
        const regex = /\(([^)]+)\)/g;

        // Replace the parentheses content with a clickable span.
        error.innerHTML = text.replace(regex, (match) => {
            const fullText = match;
            const collapsedText = '(...)';
            return `<span class="collapsible-text" data-full-text="${fullText}" data-collapsed="true"">${collapsedText}</span>`;
        });

        // Add click event listener to the created spans.
        const collapsibles = error.querySelectorAll('.collapsible-text');
        collapsibles.forEach((span) => {
            span.addEventListener('click', toggleCollapse);
        });
    });
});

// Toggles the collapsed/expanded state.
function toggleCollapse(event) {
    const span = event.target;
    const fullText = span.dataset.fullText;
    const collapsed = span.dataset.collapsed === 'true';
    const collapsedText = '(...)';

    // Toggle between the full and collapsed text.
    span.textContent = collapsed ? fullText : collapsedText;
    span.dataset.collapsed = (!collapsed).toString();
}