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

    getDisplayPageNumber(page: HTMLElement): string {
        return page.dataset.displayPageNumber ?? this.getPageNumber(page).toString();
    }

    getPageType(): "left" | "right" {
        return "right";
    }

    getPage(element: HTMLElement): HTMLElement | undefined {
        return this.pages.find(page => page.contains(element));
    }

    setDisplayPageNumber(page: HTMLElement, pageNumber: string): void {
        page.setAttribute("data-display-page-number", pageNumber);
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
        document.body.className = 'quarkdown quarkdown-paged';
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

    it('injects reset-aware numbers into table of contents entries for paged documents', async () => {
        document.body.className = 'quarkdown quarkdown-paged';
        document.body.innerHTML = `
      <div class="pagedjs_page" data-page-number="1">
        <div class="pagedjs_area">
          <h1 id="table-of-contents">Contents</h1>
          <nav data-role="table-of-contents">
            <ol>
              <li><a href="#section-1">Section 1</a></li>
              <li><a href="#section-2">Section 2</a></li>
            </ol>
          </nav>
          <h2 id="section-1">Section 1</h2>
          <span class="current-page-number">X</span>
        </div>
      </div>
      <div class="pagedjs_page" data-page-number="2">
        <div class="pagedjs_area">
          <span class="page-number-reset" data-start="5"></span>
          <h2 id="section-2">Section 2</h2>
          <span class="current-page-number">Y</span>
        </div>
      </div>`;

        const pages = Array.from(document.querySelectorAll<HTMLElement>('.pagedjs_page'));
        const handler = new Concrete(new DummyDocument(pages));

        await handler.onPostRendering();

        const numbers = Array.from(document.querySelectorAll<HTMLSpanElement>('.toc-page-number')).map(span => span.textContent);
        expect(numbers).toEqual(['1', '5']);
    });
});
