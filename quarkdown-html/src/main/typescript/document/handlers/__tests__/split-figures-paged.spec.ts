import {beforeEach, describe, expect, it} from 'vitest';
import {SplitFiguresPaged} from "../paged/split-figures-paged";
import {QuarkdownDocument} from "../../quarkdown-document";
import {ConditionalDocumentHandler} from "../../document-handler";

class DummyDocument implements QuarkdownDocument {
    getParentViewport(): HTMLElement | undefined {
        return undefined;
    }

    setupPreRenderingHook(): void {
    }

    setupPostRenderingHook(): void {
    }

    initializeRendering(): void {
    }

    getHandlers(): ConditionalDocumentHandler[] {
        return [];
    }
}

describe('SplitFiguresPaged', () => {
    beforeEach(() => {
        document.body.innerHTML = '';
    });

    it('moves a top caption to the first portion', async () => {
        document.body.innerHTML = `
      <figure data-ref="f1" data-split-to="f1">
        <pre><code>A</code></pre>
      </figure>
      <figure data-ref="f1" data-split-from="f1">
        <pre><code>B</code></pre>
        <figcaption class="caption-top">My caption</figcaption>
      </figure>`;

        await new SplitFiguresPaged(new DummyDocument()).onPostRendering();

        const [original, split] = Array.from(document.querySelectorAll('figure'));
        expect(original.querySelectorAll('figcaption').length).toBe(1);
        expect(split.querySelectorAll('figcaption').length).toBe(0);
    });

    it('keeps a bottom caption on the last portion', async () => {
        document.body.innerHTML = `
      <figure data-ref="f1" data-split-to="f1">
        <pre><code>A</code></pre>
      </figure>
      <figure data-ref="f1" data-split-from="f1">
        <pre><code>B</code></pre>
        <figcaption class="caption-bottom">My caption</figcaption>
      </figure>`;

        await new SplitFiguresPaged(new DummyDocument()).onPostRendering();

        const [original, split] = Array.from(document.querySelectorAll('figure'));
        expect(original.querySelectorAll('figcaption').length).toBe(0);
        expect(split.querySelectorAll('figcaption').length).toBe(1);
    });

    it('moves a bottom caption from a middle portion to the last one', async () => {
        document.body.innerHTML = `
      <figure data-ref="f1" data-split-to="f1">
        <pre><code>A</code></pre>
      </figure>
      <figure data-ref="f1" data-split-from="f1" data-split-to="f1">
        <pre><code>B</code></pre>
        <figcaption class="caption-bottom">My caption</figcaption>
      </figure>
      <figure data-ref="f1" data-split-from="f1">
        <pre><code>C</code></pre>
      </figure>`;

        await new SplitFiguresPaged(new DummyDocument()).onPostRendering();

        const figures = Array.from(document.querySelectorAll('figure'));
        expect(figures[0].querySelectorAll('figcaption').length).toBe(0);
        expect(figures[1].querySelectorAll('figcaption').length).toBe(0);
        expect(figures[2].querySelectorAll('figcaption').length).toBe(1);
    });

    it('moves a top caption to the first portion with content, dropping the empty one', async () => {
        document.body.innerHTML = `
      <figure data-ref="f1" data-split-to="f1">
        <figcaption class="caption-top">My caption</figcaption>
      </figure>
      <figure data-ref="f1" data-split-from="f1">
        <pre><code>A</code></pre>
      </figure>`;

        await new SplitFiguresPaged(new DummyDocument()).onPostRendering();

        const figures = Array.from(document.querySelectorAll('figure'));
        expect(figures.length).toBe(1);
        expect(figures[0].querySelector('pre')).not.toBeNull();
        expect(figures[0].querySelectorAll('figcaption').length).toBe(1);
    });

    it('keeps a caption with an image-only portion', async () => {
        document.body.innerHTML = `
      <figure data-ref="f1" data-split-to="f1">
        <img src="image.png" alt="" />
      </figure>
      <figure data-ref="f1" data-split-from="f1">
        <figcaption class="caption-top">My caption</figcaption>
      </figure>`;

        await new SplitFiguresPaged(new DummyDocument()).onPostRendering();

        const figures = Array.from(document.querySelectorAll('figure'));
        expect(figures.length).toBe(1);
        expect(figures[0].querySelector('img')).not.toBeNull();
        expect(figures[0].querySelectorAll('figcaption').length).toBe(1);
    });

    it('removes the page of a dropped portion that was its only content', async () => {
        document.body.innerHTML = `
      <div class="pagedjs_page"><div class="pagedjs_area"><div class="pagedjs_page_content"><div>
        <figure data-ref="f1" data-split-to="f1">
          <pre><code>A</code></pre>
        </figure>
      </div></div><div class="pagedjs_footnote_area"></div></div></div>
      <div class="pagedjs_page"><div class="pagedjs_area"><div class="pagedjs_page_content"><div>
        <figure data-ref="f1" data-split-from="f1">
          <figcaption class="caption-bottom">My caption</figcaption>
        </figure>
      </div></div><div class="pagedjs_footnote_area"></div></div></div>`;

        await new SplitFiguresPaged(new DummyDocument()).onPostRendering();

        // The caption moved to the content portion; the caption-only page is gone.
        expect(document.querySelectorAll('.pagedjs_page').length).toBe(1);
        expect(document.querySelectorAll('figcaption').length).toBe(1);
        expect(document.querySelector('figure')?.querySelector('pre')).not.toBeNull();
    });

    it('keeps the page of a dropped portion that had sibling content', async () => {
        document.body.innerHTML = `
      <div class="pagedjs_page"><div class="pagedjs_area"><div class="pagedjs_page_content"><div>
        <figure data-ref="f1" data-split-to="f1">
          <pre><code>A</code></pre>
        </figure>
      </div></div></div></div>
      <div class="pagedjs_page"><div class="pagedjs_area"><div class="pagedjs_page_content"><div>
        <figure data-ref="f1" data-split-from="f1">
          <figcaption class="caption-bottom">My caption</figcaption>
        </figure>
        <p>Trailing paragraph</p>
      </div></div></div></div>`;

        await new SplitFiguresPaged(new DummyDocument()).onPostRendering();

        // Only the empty portion is removed: the page keeps its other content.
        expect(document.querySelectorAll('.pagedjs_page').length).toBe(2);
        expect(document.querySelector('p')).not.toBeNull();
    });

    it('leaves the caption untouched when no portion has content', async () => {
        document.body.innerHTML = `
      <figure data-ref="f1" data-split-to="f1">
        <figcaption class="caption-top">My caption</figcaption>
      </figure>
      <figure data-ref="f1" data-split-from="f1">
        <pre><code></code></pre>
      </figure>`;

        await new SplitFiguresPaged(new DummyDocument()).onPostRendering();

        const figures = Array.from(document.querySelectorAll('figure'));
        expect(figures.length).toBe(2);
        expect(figures[0].querySelectorAll('figcaption').length).toBe(1);
    });

    it('ignores figures that were not split', async () => {
        document.body.innerHTML = `
      <figure data-ref="f1">
        <pre><code>A</code></pre>
        <figcaption class="caption-top">My caption</figcaption>
      </figure>`;

        await new SplitFiguresPaged(new DummyDocument()).onPostRendering();

        const figure = document.querySelector('figure')!;
        expect(figure.querySelectorAll('figcaption').length).toBe(1);
    });
});
