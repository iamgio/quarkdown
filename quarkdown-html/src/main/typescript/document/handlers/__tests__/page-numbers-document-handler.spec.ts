import {describe, expect, it} from 'vitest';
import {PageNumbers} from "../page-numbers";
import {PagedLikeQuarkdownDocument} from "../../paged-like-quarkdown-document";

class DummyDocument implements PagedLikeQuarkdownDocument<HTMLElement> {
    constructor(private readonly pages: HTMLElement[]) {
    }

    getPages(): HTMLElement[] {
        return this.pages;
    }

    getPageNumber(page: HTMLElement): number {
        return parseInt(page.dataset.pageNumber || "0", 10);
    }

    getPageType(): "left" | "right" {
        return "right";
    }

    getParentViewport(): HTMLElement | undefined {
        return undefined;
    }

    setupPreRenderingHook(): void {
    }

    setupPostRenderingHook(): void {
    }

    initializeRendering(): void {
    }

    getHandlers() {
        return [];
    }
}

class Concrete extends PageNumbers {
}

describe('PageNumbersDocumentHandler', () => {
    it('respects page number reset markers when numbering pages', async () => {
        document.body.innerHTML = `
      <div class="pagedjs_page" data-page-number="1">
        <span class="current-page-number">X</span>
      </div>
      <div class="pagedjs_page" data-page-number="2">
        <span class="page-number-reset" data-start="10"></span>
        <span class="current-page-number">Y</span>
      </div>
      <div class="pagedjs_page" data-page-number="3">
        <span class="current-page-number">Z</span>
      </div>`;

        const pages = Array.from(document.querySelectorAll<HTMLElement>('.pagedjs_page'));
        const handler = new Concrete(new DummyDocument(pages));

        await handler.onPostRendering();

        const numbers = pages.map(page => page.querySelector('.current-page-number')?.textContent);
        expect(numbers).toEqual(['1', '10', '11']);
        expect(pages.map(page => page.dataset.displayPageNumber)).toEqual(['1', '10', '11']);
        const totals = Array.from(document.querySelectorAll('.total-page-number'));
        expect(totals.length).toBe(0);
    });
});
