import {QuarkdownDocument} from "./quarkdown-document";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../queue/execution-queues";

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
     * Hook called before document rendering begins,
     * via the pre-rendering execution queue.
     */
    onPreRendering?(): void

    /**
     * Hook called after document rendering completes,
     * via the post-rendering execution queue.
     */
    onPostRendering?(): void

    /**
     * Pushes this handler's lifecycle methods to the appropriate execution queues.
     * Pre-rendering handlers are added to the pre-rendering queue,
     * post-rendering handlers are added to the post-rendering queue.
     */
    pushToQueue() {
        if (this.onPreRendering) {
            preRenderingExecutionQueue.push(() => this.onPreRendering!());
        }
        if (this.onPostRendering) {
            postRenderingExecutionQueue.push(() => this.onPostRendering!());
        }
    }
}