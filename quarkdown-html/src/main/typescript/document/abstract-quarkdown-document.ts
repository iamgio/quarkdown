import {FootnoteDefinitionAndReference, QuarkdownDocument} from "./quarkdown-document";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../queue/execution-queues";

/**
 * A Quarkdown document, inherited by each specific document type.
 *
 * This class manages the document lifecycle, including pre-rendering and post-rendering
 * execution queues, page margin content, persistent headings, footnotes, and navigation.
 *
 * @example
 * ```typescript
 * const doc = new QuarkdownDocument();
 * doc.prepare(); // Sets up the document runtime stages
 * ```
 */
export abstract class AbstractQuarkdownDocument implements QuarkdownDocument {
    /** Elements that will be copied into each section and placed on page margins */
    protected pageMarginInitializers?: NodeListOf<Element>;

    prepare(): void {
        this.populateExecutionQueue();
        this.setupBeforeReadyHook();
        this.setupAfterReadyHook();
    }

    beforeReady(content: Document | Element): void {
        this.pageMarginInitializers = content.querySelectorAll('.page-margin-content');
    }

    removeAllPageMarginInitializers(): void {
        if (!this.pageMarginInitializers) return;

        this.pageMarginInitializers.forEach(initializer => initializer.remove());
    }

    copyPageMarginInitializers(): void {
        if (!this.pageMarginInitializers) {
            console.error('pageMarginInitializers not set');
            return;
        }
    }

    populateExecutionQueue(): void {
        postRenderingExecutionQueue.push(() => this.copyPageMarginInitializers());
        postRenderingExecutionQueue.push(() => this.copyPersistentHeadings());
        postRenderingExecutionQueue.push(() => this.updatePageNumberElements());
        postRenderingExecutionQueue.push(() => this.handleFootnotes(getFootnoteDefinitionsAndFirstReference()));
        if (this.usesNavigationSidebar()) {
            postRenderingExecutionQueue.push(createSidebar);
        }
    }

    usesNavigationSidebar(): boolean {
        return true;
    }

    setupBeforeReadyHook(): void {
        document.addEventListener('DOMContentLoaded', async () => {
            await preRenderingExecutionQueue.execute();
            this.beforeReady(document);
        });
    }

    setupAfterReadyHook(): void {
        document.addEventListener('DOMContentLoaded', () => {
            // postRenderingExecutionQueue.execute()
        });
    }

    abstract copyPersistentHeadings(): void;

    abstract handleFootnotes(footnotes: FootnoteDefinitionAndReference[]): void;

    abstract getParentViewport(element: Element): Element;

    abstract updatePageNumberElements(): void;
}
