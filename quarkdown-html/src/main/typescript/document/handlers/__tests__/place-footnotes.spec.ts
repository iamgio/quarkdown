import {beforeEach, describe, expect, it} from 'vitest';
import {PlaceFootnotes} from "../paged/node/place-footnotes";
import {DetachedFootnotes} from "../footnotes/detached-footnotes";

/** Markup of an empty paged.js page, as the page template renders it. */
function pageMarkup(id: string): string {
    return `
      <div class="pagedjs_page" id="${id}"><div class="pagedjs_area">
        <div class="pagedjs_page_content"><div></div></div>
        <div class="pagedjs_footnote_area">
          <div class="pagedjs_footnote_content pagedjs_footnote_empty">
            <div class="pagedjs_footnote_inner_content"></div>
          </div>
        </div>
      </div></div>`;
}

/** Stubs a layout read that happy-dom does not compute. */
function stub(element: Element, property: string, value: number | (() => Partial<DOMRect>)) {
    Object.defineProperty(element, property, {get: () => value, configurable: true});
}

function area(pageId: string): HTMLElement {
    return document.querySelector<HTMLElement>(`#${pageId} .pagedjs_area`)!;
}

function footnoteContent(pageId: string): HTMLElement {
    return area(pageId).querySelector<HTMLElement>('.pagedjs_footnote_content')!;
}

describe('PlaceFootnotes', () => {
    let handler: PlaceFootnotes;
    let definition: HTMLElement;
    let source: HTMLElement;

    beforeEach(() => {
        document.body.innerHTML = pageMarkup('page-1') + pageMarkup('page-2');
        source = document.createElement('p');
        source.innerHTML = 'Text<sup class="footnote-reference" data-definition="def-1">1</sup> more';
        definition = document.createElement('span');
        definition.className = 'footnote-definition';
        definition.id = 'def-1';
        definition.textContent = 'Note';
        const detached = new DetachedFootnotes();
        detached.detach([{reference: source.querySelector('.footnote-reference')!, definition}]);
        handler = new PlaceFootnotes(detached);
        for (const pageId of ['page-1', 'page-2']) {
            stub(footnoteContent(pageId), 'scrollHeight', 40);
            stub(area(pageId), 'clientHeight', 1000);
        }
    });

    /** Clones the source paragraph into the given page, as paged.js does, and lets the handler render it. */
    function renderInto(pageId: string): HTMLElement {
        const clone = source.cloneNode(true) as HTMLElement;
        area(pageId).querySelector('.pagedjs_page_content > div')!.appendChild(clone);
        expect(handler.accepts(clone)).toBe(true);
        handler.render(clone, source);
        return clone;
    }

    it('accepts only nodes holding an anchor reference', () => {
        expect(handler.accepts(document.createTextNode('text'))).toBe(false);
        const plain = document.createElement('p');
        plain.innerHTML = '<sup class="footnote-reference" data-definition="def-1">1</sup>';
        expect(handler.accepts(plain)).toBe(false);
        expect(handler.accepts(source)).toBe(true);
    });

    it('places the definition into the footnote area of the page of its anchor', () => {
        renderInto('page-1');

        expect(definition.closest('.pagedjs_page')!.id).toBe('page-1');
        expect(definition.parentElement!.className).toBe('pagedjs_footnote_inner_content');
        expect(definition.previousElementSibling!.className).toBe('footnote-rule');
        expect(footnoteContent('page-1').classList.contains('pagedjs_footnote_empty')).toBe(false);
        expect(area('page-1').style.getPropertyValue('--pagedjs-footnotes-height')).toBe('40px');
    });

    it('does not move the definition when its anchor is rendered again on the same page', () => {
        renderInto('page-1');
        const inner = definition.parentElement;

        renderInto('page-1');

        expect(definition.parentElement).toBe(inner);
    });

    it('moves the definition along when its anchor is rendered on a later page', () => {
        renderInto('page-1');

        renderInto('page-2');

        expect(definition.closest('.pagedjs_page')!.id).toBe('page-2');
        expect(area('page-2').style.getPropertyValue('--pagedjs-footnotes-height')).toBe('40px');
        expect(area('page-1').style.getPropertyValue('--pagedjs-footnotes-height')).toBe('0px');
        expect(footnoteContent('page-1').classList.contains('pagedjs_footnote_empty')).toBe(true);
        expect(footnoteContent('page-1').querySelector('.footnote-rule')).toBeNull();
    });

    it('ignores anchors rendered in the overflow column', () => {
        const content = area('page-1').querySelector<HTMLElement>('.pagedjs_page_content')!;
        stub(content, 'getBoundingClientRect', () => ({left: 0, right: 400, width: 400}));
        const clone = source.cloneNode(true) as HTMLElement;
        content.firstElementChild!.appendChild(clone);
        stub(clone.querySelector('.footnote-reference')!, 'getBoundingClientRect', () => ({left: 1400}));

        handler.render(clone, source);

        expect(definition.isConnected).toBe(false);
    });

    it('includes the vertical margins of the footnote content in the reserved height, like paged.js', () => {
        const content = footnoteContent('page-1');
        content.style.marginTop = '10px';
        content.style.marginBottom = '4px';

        renderInto('page-1');

        expect(area('page-1').style.getPropertyValue('--pagedjs-footnotes-height')).toBe('54px');
    });

    it('caps the footnote area at half the page area', () => {
        stub(area('page-1'), 'clientHeight', 60);

        renderInto('page-1');

        expect(area('page-1').style.getPropertyValue('--pagedjs-footnotes-height')).toBe('30px');
    });
});
