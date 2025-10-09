// Main entry point for the quarkdown-html runtime.

import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "./queue/execution-queues";

let doc = new QuarkdownDocument(); // Overwritten externally by html-wrapper

/**
 * Returns whether the document is finalized and ready.
 * This can be watched and waited for by other tools, such as Puppeteer to generate a PDF.
 * @returns {boolean}
 */
export function isReady() {
    return preRenderingExecutionQueue.isCompleted() && postRenderingExecutionQueue.isCompleted();
}