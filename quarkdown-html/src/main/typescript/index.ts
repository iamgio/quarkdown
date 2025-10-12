// Main entry point for the quarkdown-html runtime.

import {capabilities} from "./capabilities";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "./queue/execution-queues";
import {PlainDocument} from "./document/type/plain-document";
import {prepare} from "./document/quarkdown-document";
import {notifyLivePreview} from "./live/live-preview";
import {SlidesDocument} from "./document/type/slides-document";
import {PagedDocument} from "./document/type/paged-document";

/**
 * Returns whether the document is finalized and ready.
 * This can be watched and waited for by other tools, such as Puppeteer to generate a PDF.
 * @returns {boolean}
 */
function isReady(): boolean {
    return preRenderingExecutionQueue.isCompleted() && postRenderingExecutionQueue.isCompleted();
}

// Notify the live preview that the document is ready after pre-rendering tasks are done.
postRenderingExecutionQueue.addOnComplete(() => notifyLivePreview('postRenderingCompleted'));

// Expose the API to the global context.
const context = window as any;
context.isReady = isReady;
context.quarkdownCapabilities = capabilities;
context.prepare = prepare;
context.PlainDocument = PlainDocument;
context.PagedDocument = PagedDocument;
context.SlidesDocument = SlidesDocument;
