import {describe, expect, it} from 'vitest';
import {PageMarginsDocumentHandler} from "../page-margins/page-margins-document-handler";

class DummyDoc {
}

class Concrete extends PageMarginsDocumentHandler {
    apply() {
        // no-op for test
    }
}

describe('PageMarginsDocumentHandler', () => {
    it('collects and removes page margin initializers on pre-rendering', async () => {
        document.body.innerHTML = `
      <div>
        <div class="page-margin-content" id="a"></div>
        <div class="page-margin-content" id="b"></div>
        <div class="other"></div>
      </div>`;

        const handler = new Concrete(new DummyDoc() as any);
        await handler.onPreRendering();

        // @ts-expect-error access for testing
        const collected = handler.pageMarginInitializers as HTMLElement[];
        expect(collected.map(e => e.id).sort()).toEqual(['a', 'b']);
        // elements removed from DOM
        expect(document.querySelectorAll('.page-margin-content').length).toBe(0);
    });
});
