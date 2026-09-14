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
