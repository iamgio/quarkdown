import {DocumentHandler} from "../document-handler";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../../queue/execution-queues";
import {Sidebar} from "../handlers/sidebar";
import {PageMarginsPaged} from "../handlers/page-margins/page-margins-paged";
import {FootnotesPaged} from "../handlers/footnotes/footnotes-paged";
import {SplitCodeBlocksFixPaged} from "../handlers/paged/split-code-blocks-fix-paged";
import {ColumnCountPaged} from "../handlers/paged/column-count-paged";
import {PageNumbers} from "../handlers/page-numbers";
import {PagedLikeQuarkdownDocument} from "../paged-like-quarkdown-document";
import {ShowOnReady} from "../handlers/show-on-ready";
import {PersistentHeadings} from "../handlers/persistent-headings";

declare const Paged: typeof import("pagedjs"); // global Paged at runtime

/**
 * Paged document implementation for paged.js media.
 */
export class PagedDocument implements PagedLikeQuarkdownDocument {
    /**
     * @returns The parent page of the given element.
     */
    getParentViewport(element: Element): HTMLElement | undefined {
        return element.closest<HTMLElement>('.pagedjs_area') || undefined;
    }

    getPages(): HTMLElement[] {
        return Array.from(document.querySelectorAll<HTMLElement>('.pagedjs_page'));
    }

    getPageNumber(page: HTMLElement): number {
        return parseInt(page.dataset.pageNumber || "0");
    }

    getPageType(page: HTMLElement): "left" | "right" {
        return page.classList.contains("pagedjs_right_page") ? "right" : "left";
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
            new ShowOnReady(this),
            new PageMarginsPaged(this),
            new PageNumbers(this),
            new PersistentHeadings(this),
            new FootnotesPaged(this),
            new ColumnCountPaged(this),
            new SplitCodeBlocksFixPaged(this),
        ];
    }
}
