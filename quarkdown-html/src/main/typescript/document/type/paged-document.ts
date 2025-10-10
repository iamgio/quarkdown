import {QuarkdownDocument} from "../quarkdown-document";
import {DocumentHandler} from "../document-handler";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../../queue/execution-queues";
import {Sidebar} from "../handlers/sidebar";
import {PageNumbersPaged} from "../handlers/page-numbers/page-numbers-paged";

declare const Paged: typeof import("pagedjs"); // global Paged at runtime

/**
 * Paged document implementation for paged.js media.
 */
export class PagedDocument implements QuarkdownDocument {
    /**
     * @returns The parent page of the given element.
     */
    getParentViewport(element: Element): Element | undefined {
        return element.closest('.pagedjs_area') || undefined;
    }

    /** Sets up pre-rendering to execute when DOM content is loaded. */
    setupPreRenderingHook() {
        document.addEventListener("DOMContentLoaded", async () => await preRenderingExecutionQueue.execute());
    }

    /** Sets up post-rendering to execute when paged.js is ready. */
    setupPostRenderingHook(): void {
        class PagedAfterReadyHandler extends Paged.Handler {
            afterRendered() {
                postRenderingExecutionQueue.execute().then();
            }
        }
        Paged.registerHandlers(PagedAfterReadyHandler);
    }

    /** Initializes paged.js rendering. */
    initializeRendering(): void {
        (window as any).PagedPolyfill?.preview().then();
    }

    getHandlers(): DocumentHandler[] {
        return [
            new Sidebar(this),
            new PageNumbersPaged(this),
        ];
    }
}
