import {QuarkdownDocument} from "./quarkdown-document";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../queue/execution-queues";

/**
 * Type representing either a `DocumentHandler` instance or a boolean flag.
 * Used for conditional inclusion of document handlers.
 * Only `DocumentHandler` instances are retained for processing.
 * @see filterConditionalHandlers
 */
export type ConditionalDocumentHandler = DocumentHandler | boolean;

/**
 * Filters an array of `ConditionalDocumentHandler` to retain only actual `DocumentHandler` instances.
 * @param handlers - Array of `ConditionalDocumentHandler` (either `DocumentHandler` or boolean)
 * @returns Array of `DocumentHandler` instances
 */
export function filterConditionalHandlers(handlers: ConditionalDocumentHandler[]): DocumentHandler[] {
    return handlers.filter((handler): handler is DocumentHandler => handler instanceof DocumentHandler);
}

/**
 * Source of an event or action related to document processing,
 * with hooks for pre-rendering and post-rendering phases.
 */
export abstract class DocumentHandler {
    /**
     * @param quarkdownDocument - The document instance this handler manages
     */
    constructor(protected readonly quarkdownDocument: QuarkdownDocument) {
    }

    /**
     * Optional initialization hook called when the handler is created.
     */
    init?(): void

    /**
     * Hook called before document rendering begins,
     * via the pre-rendering execution queue.
     */
    async onPreRendering?(): Promise<void>

    /**
     * Hook called after document rendering completes,
     * via the post-rendering execution queue.
     */
    async onPostRendering?(): Promise<void>

    /**
     * Pushes this handler's lifecycle methods to the appropriate execution queues.
     * Pre-rendering handlers are added to the pre-rendering queue,
     * post-rendering handlers are added to the post-rendering queue.
     */
    pushToQueue() {
        this.init?.();
        if (this.onPreRendering) {
            preRenderingExecutionQueue.pushAsync(() => this.onPreRendering!());
        }
        if (this.onPostRendering) {
            postRenderingExecutionQueue.pushAsync(() => this.onPostRendering!());
        }
    }
}