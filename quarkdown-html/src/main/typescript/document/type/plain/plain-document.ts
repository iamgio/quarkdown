import {QuarkdownDocument} from "../../quarkdown-document";
import {DocumentHandler} from "../../document-handler";
import {SidebarDocumentHandler} from "../../handlers/sidebar-document-handler";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../../../queue/execution-queues";

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
            new SidebarDocumentHandler(this),
        ];
    }
}
