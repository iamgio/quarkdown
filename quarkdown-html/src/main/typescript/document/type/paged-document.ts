import {DocumentHandler} from "../document-handler";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../../queue/execution-queues";
import {Sidebar} from "../handlers/sidebar";
import {PageMarginsPaged} from "../handlers/page-margins/page-margins-paged";
import {FootnotesPaged} from "../handlers/footnotes/footnotes-paged";
import {SplitTablesPaged} from "../handlers/paged/split-tables-paged";
import {SplitFiguresPaged} from "../handlers/paged/split-figures-paged";
import {HeadingBreaksPaged} from "../handlers/paged/heading-breaks-paged";
import {PagedNodeHandler} from "../handlers/paged/node/paged-node-handler";
import {RepeatTableHeaders} from "../handlers/paged/node/repeat-table-headers";
import {SplitOversizedTableRows} from "../handlers/paged/node/split-oversized-table-rows";
import {RestoreCodeIndentation} from "../handlers/paged/node/restore-code-indentation";
import {PageNumbers} from "../handlers/page-numbers";
import {PagedLikeQuarkdownDocument} from "../paged-like-quarkdown-document";
import {ShowOnReady} from "../handlers/show-on-ready";
import {PersistentHeadings} from "../handlers/persistent-headings";
import {createTimeSlicedTick} from "../../queue/time-sliced-tick";
import {snapOverflowToWordStart} from "../handlers/paged/word-boundary-breaks";
import {DetachedFootnotes} from "../handlers/footnotes/detached-footnotes";
import {PlaceFootnotes} from "../handlers/paged/node/place-footnotes";

declare const Paged: typeof import("pagedjs"); // global Paged at runtime

/**
 * Paged document implementation for paged.js media.
 */
export class PagedDocument implements PagedLikeQuarkdownDocument {
    /** Footnote definitions taken out of the content flow, placed into pages during pagination. */
    private readonly footnotes = new DetachedFootnotes();

    /**
     * @returns The parent page of the given element.
     */
    getParentViewport(element: Element): HTMLElement | undefined {
        return element.closest<HTMLElement>('.pagedjs_area') || undefined;
    }

    getPages(): HTMLElement[] {
        return Array.from(document.querySelectorAll<HTMLElement>('.pagedjs_page'));
    }

    getPage(element: HTMLElement): HTMLElement | undefined {
        return element.closest<HTMLElement>('.pagedjs_page') || undefined;
    }

    getPageNumber(page: HTMLElement): number {
        return parseInt(page.dataset.pageNumber ?? "0");
    }

    getDisplayPageNumber(page: HTMLElement): string {
        return page.dataset.displayPageNumber ?? this.getPageNumber(page).toString();
    }

    setDisplayPageNumber(page: HTMLElement, pageNumber: string) {
        page.setAttribute("data-display-page-number", pageNumber);
    }

    getPageType(page: HTMLElement): "left" | "right" {
        return page.classList.contains("pagedjs_right_page") ? "right" : "left";
    }

    /**
     * Sets up pre-rendering to execute when DOM content is loaded,
     * and registers the paged.js handlers that take part in pagination:
     * the [node handlers][getNodeHandlers] and the snapping of page breaks to word boundaries.
     */
    setupPreRenderingHook() {
        document.addEventListener("DOMContentLoaded", async () => await preRenderingExecutionQueue.execute());

        const nodeHandlers = this.getNodeHandlers();
        class PagedNodeHandlerDispatcher extends Paged.Handler {
            renderNode(clone: Node, source: Node) {
                nodeHandlers
                    .filter(handler => handler.accepts(clone))
                    .forEach(handler => handler.render(clone, source));
            }
        }

        class WordBoundaryBreaks extends Paged.Handler {
            onOverflow(overflow: Range | undefined, rendered: Element, bounds: DOMRect) {
                return overflow && snapOverflowToWordStart(overflow, bounds.right);
            }
        }

        Paged.registerHandlers(PagedNodeHandlerDispatcher, WordBoundaryBreaks);
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

    /**
     * Initializes paged.js rendering.
     */
    initializeRendering(): void {
        const previewer = (window as any).PagedPolyfill;
        if (!previewer) return;


        // paged.js lays out one page per animation frame by default, idling until the next frame after each page.
        // Its queue is given a time-sliced tick instead, which lays out pages back-to-back and yields to rendering periodically.
        previewer.chunker.q.tick = createTimeSlicedTick();
        previewer.preview().then();
    }

    getHandlers(): DocumentHandler[] {
        return [
            new Sidebar(this),
            new ShowOnReady(this),
            new HeadingBreaksPaged(this),
            new SplitTablesPaged(this),
            new SplitFiguresPaged(this),
            new PageMarginsPaged(this),
            new PageNumbers(this),
            new PersistentHeadings(this),
            new FootnotesPaged(this, this.footnotes),
        ];
    }

    /**
     * @returns Handlers of single nodes rendered during pagination.
     */
    getNodeHandlers(): PagedNodeHandler[] {
        return [
            new RepeatTableHeaders(),
            new SplitOversizedTableRows(),
            new RestoreCodeIndentation(),
            new PlaceFootnotes(this.footnotes),
        ];
    }
}
