import {beforeEach, describe, expect, it} from 'vitest';
import {FootnotesPaged} from "../footnotes/footnotes-paged";
import {DetachedFootnotes, FOOTNOTE_ANCHOR_ATTRIBUTE} from "../footnotes/detached-footnotes";

describe('FootnotesPaged', () => {
    let detached: DetachedFootnotes;
    let secondDefinition: HTMLElement;

    beforeEach(async () => {
        detached = new DetachedFootnotes();
        document.body.innerHTML = `
      <main>
        <p>A<span class="footnote-reference" data-definition="def-1">1</span></p>
        <div class="footnote-definition" id="def-1" data-footnote-index="1">First</div>
        <p>B<span class="footnote-reference" data-definition="def-2">2</span>
           again<span class="footnote-reference" data-definition="def-1">1</span></p>
        <div class="footnote-definition" id="def-2" data-footnote-index="2">Second</div>
      </main>`;
        secondDefinition = document.getElementById('def-2')!;

        await new FootnotesPaged({} as any, detached).onPreRendering();
    });

    it('detaches definitions from the content flow on pre-rendering', () => {
        expect(document.querySelectorAll('.footnote-definition')).toHaveLength(0);
    });

    it('marks only the first reference of each definition as its anchor', () => {
        const references = Array.from(document.querySelectorAll('.footnote-reference'));
        expect(references.map(r => r.hasAttribute(FOOTNOTE_ANCHOR_ATTRIBUTE))).toEqual([true, true, false]);
    });

    it('keeps references in the inline flow', () => {
        const reference = document.querySelector<HTMLElement>('.footnote-reference')!;
        expect(reference.style.display).toBe('');
        expect(reference.style.height).toBe('');
    });

    it('hands detached definitions over by their anchor', () => {
        const anchor = document.querySelector('.footnote-reference[data-definition="def-2"]')!;
        expect(detached.definitionOf(anchor)).toBe(secondDefinition);
    });
});
