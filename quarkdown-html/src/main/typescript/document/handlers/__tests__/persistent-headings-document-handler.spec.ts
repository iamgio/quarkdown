import {describe, expect, it} from 'vitest';
import {PersistentHeadings} from "../persistent-headings";
import {PagedLikeQuarkdownDocument} from "../../paged-like-quarkdown-document";
import {ConditionalDocumentHandler} from "../../document-handler";
import {prepare} from "../../quarkdown-document";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../../../queue/execution-queues";

class DummyDoc implements PagedLikeQuarkdownDocument {
    getHandlers(): ConditionalDocumentHandler[] {
        return [new PersistentHeadings(this)];
    }

    getParentViewport(element: Element): HTMLElement | undefined {
        return document.documentElement;
    }

    initializeRendering(): void {
        postRenderingExecutionQueue.execute().then();
    }

    setupPostRenderingHook(): void {
    }

    setupPreRenderingHook(): void {
        preRenderingExecutionQueue.execute().then();
    }

    getPageNumber(page: HTMLElement): number {
        return 0;
    }

    getPageType(_: HTMLElement): "left" | "right" {
        return "left";
    }

    getPages(): HTMLElement[] {
        return [document.documentElement];
    }
}

describe('PersistentHeadingsDocumentHandler', () => {
    it('stores last heading per depth and applies to .last-heading', () => {
        // Build a page with two containers: source and target
        const container = document.createElement('div');
        container.innerHTML = `
      <h1>Main</h1>
      <h2>Section A</h2>
      <h3 data-decorative>Decor</h3>
      <h2>Section B</h2>
      <div class="last-heading" data-depth="1"></div>
      <div class="last-heading" data-depth="2"></div>
      <div class="last-heading" data-depth="3"></div>`;

        document.body.appendChild(container);

        postRenderingExecutionQueue.addOnComplete(() => {
            const [h1, h2, h3] = Array.from(container.querySelectorAll('.last-heading')) as HTMLElement[];
            expect(h1.innerHTML).toBe('Main');
            expect(h2.innerHTML).toBe('Section B');
            expect(h3.innerHTML).toBe(''); // no h3 persisted because decorative + cleared after depth 2
        });
        prepare(new DummyDoc());
    });
});
