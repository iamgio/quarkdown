// Main entry point for the quarkdown-html runtime.

import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "./queue/execution-queues";
import {PlainDocument} from "./document/type/plain/plain-document";
import {prepare} from "./document/quarkdown-document";

// noinspection JSUnusedGlobalSymbols
/**
 * Returns whether the document is finalized and ready.
 * This can be watched and waited for by other tools, such as Puppeteer to generate a PDF.
 * @returns {boolean}
 */
export function isReady(): boolean {
    return preRenderingExecutionQueue.isCompleted() && postRenderingExecutionQueue.isCompleted();
}

const context = window as any;
context.prepare = prepare;
context.PlainDocument = PlainDocument;
