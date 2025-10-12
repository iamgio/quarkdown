import {describe, expect, it} from 'vitest';
import {FootnotesDocumentHandler} from "../footnotes/footnotes-document-handler";

class DummyDoc {
}

class Concrete extends FootnotesDocumentHandler {
}

describe('FootnotesDocumentHandler', () => {
    it('collects footnote pairs on pre-rendering', async () => {
        document.body.innerHTML = `
      <div>
        <span class="footnote-reference" data-definition="def-1">[1]</span>
        <span class="footnote-reference" data-definition="def-1">[1.2]</span>
        <div class="footnote-definition" id="def-1" data-footnote-index="1"></div>
        <div class="footnote-definition" id="def-2" data-footnote-index="2"></div>
      </div>`;

        const handler = new Concrete(new DummyDoc() as any);
        await handler.onPreRendering();

        // @ts-expect-error access for testing protected field
        const footnotes = handler.footnotes;
        expect(footnotes.length).toBe(1);
        expect(footnotes[0].reference?.textContent).toBe('[1]');
        expect(footnotes[0].definition.id).toBe('def-1');
    });
});
