import {QuarkdownDocument} from "../quarkdown-document";
import {DocumentHandler} from "../document-handler";
import {Sidebar} from "../handlers/sidebar";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../../queue/execution-queues";
import {FootnotesPlain} from "../handlers/footnotes/footnotes-plain";

/**
 * Plain document implementation for standard HTML documents.
 * Uses the document element as the viewport and executes rendering queues sequentially.
 */
export class PlainDocument implements QuarkdownDocument {
    /**
     * @returns The document element
     */
    getParentViewport(_element: Element): Element | undefined {
        return document.documentElement;
    }

    /** Sets up pre-rendering to execute when DOM content is loaded */
    setupPreRenderingHook() {
        document.addEventListener("DOMContentLoaded", async () => {
           await preRenderingExecutionQueue.execute();
        });
    }

    /** No post-rendering hook needed for plain documents */
    setupPostRenderingHook() {
    }

    /** Executes post-rendering queue since pre- and post-rendering overlap for plain documents */
    initializeRendering() {
        postRenderingExecutionQueue.execute().then();
    }

    getHandlers(): DocumentHandler[] {
        return [
            new Sidebar(this),
            new FootnotesPlain(this),
        ];
    }
}

/**
 * Retrieves the right margin area element from the document.
 * This area is typically used for displaying footnotes or annotations.
 * @returns The right margin area, if available
 */
export function getRightMarginArea(): HTMLElement | null {
    return document.querySelector<HTMLElement>('#margin-area-right');
}