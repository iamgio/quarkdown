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
        class PagedExecutionHandler extends Paged.Handler {
            afterPreview(pages) {
                executeQueue();
            }
        }

        Paged.registerHandlers(PagedExecutionHandler);
        return;
    }

    document.addEventListener('DOMContentLoaded', executeQueue);
}