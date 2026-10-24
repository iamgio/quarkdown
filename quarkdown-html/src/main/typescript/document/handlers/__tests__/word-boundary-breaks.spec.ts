import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {snapOverflowToWordStart} from "../paged/word-boundary-breaks";

/** Right edge of the page content box; paged.js lays overflowing content out far beyond it. */
const PAGE_RIGHT = 500;

/** Builds an overflow range starting at `offset` within the last text node of the paragraph. */
function overflowAt(html: string, offset: number): {range: Range, text: Text, paragraph: HTMLElement} {
    document.body.innerHTML = html;
    const paragraph = document.querySelector<HTMLElement>('p')!;
    const text = paragraph.lastChild as Text;
    const range = document.createRange();
    range.setStart(text, offset);
    range.setEndAfter(paragraph);
    return {range, text, paragraph};
}

/** Stubs the line boxes happy-dom does not compute for the part of the word that fits on the page. */
function stubLineBoxes(rects: {top: number, left: number}[]) {
    vi.spyOn(Range.prototype, 'getClientRects').mockReturnValue(
        rects.map(rect => ({...rect, width: 40, height: 20}) as DOMRect) as unknown as DOMRectList,
    );
}

describe('snapOverflowToWordStart', () => {
    beforeEach(() => stubLineBoxes([{top: 10, left: 0}]));
    afterEach(() => vi.restoreAllMocks());

    it('moves a mid-word start back to the start of the word', () => {
        const {range, text} = overflowAt('<p>wages, for example the wages</p>', 'wages, for examp'.length);

        const snapped = snapOverflowToWordStart(range, PAGE_RIGHT)!;

        expect(snapped.startContainer).toBe(text);
        expect(snapped.startOffset).toBe('wages, for '.length);
    });

    it('keeps the end of the range', () => {
        const {range, paragraph} = overflowAt('<p>wages, for example the wages</p>', 'wages, for examp'.length);

        const snapped = snapOverflowToWordStart(range, PAGE_RIGHT)!;

        expect(snapped.endContainer).toBe(paragraph.parentNode);
        expect(snapped.endOffset).toBe(range.endOffset);
    });

    it('leaves a start at a word boundary untouched', () => {
        const {range} = overflowAt('<p>wages, for example</p>', 'wages, for '.length);

        expect(snapOverflowToWordStart(range, PAGE_RIGHT)).toBeUndefined();
    });

    it('leaves a start on whitespace untouched', () => {
        const {range} = overflowAt('<p>wages, for example</p>', 'wages, for'.length);

        expect(snapOverflowToWordStart(range, PAGE_RIGHT)).toBeUndefined();
    });

    it('leaves element starts untouched', () => {
        const {range, paragraph} = overflowAt('<p>wages, for example</p>', 0);
        range.setStart(paragraph, 0);

        expect(snapOverflowToWordStart(range, PAGE_RIGHT)).toBeUndefined();
    });

    it('keeps the break when the fitted part of the word spans several lines', () => {
        stubLineBoxes([{top: 10, left: 0}, {top: 34, left: 0}]);
        const {range} = overflowAt('<p>房东必须在次年向您提供一份详细的费用结算单</p>', 12);

        expect(snapOverflowToWordStart(range, PAGE_RIGHT)).toBeUndefined();
    });

    it('ignores boxes in the overflow column when measuring the fitted part', () => {
        stubLineBoxes([{top: 10, left: 0}, {top: 5, left: 1500}]);
        const {range, text} = overflowAt('<p>wages, for example the wages</p>', 'wages, for examp'.length);

        const snapped = snapOverflowToWordStart(range, PAGE_RIGHT)!;

        expect(snapped.startContainer).toBe(text);
        expect(snapped.startOffset).toBe('wages, for '.length);
    });

    it('treats non-breaking spaces as part of the word, like paged.js', () => {
        const {range, text} = overflowAt('<p>x a b</p>', 'x a '.length);

        const snapped = snapOverflowToWordStart(range, PAGE_RIGHT)!;

        expect(snapped.startContainer).toBe(text);
        expect(snapped.startOffset).toBe('x '.length);
    });

    it('starts before the text node when the word starts it after a separator', () => {
        const {range, paragraph} = overflowAt('<p><strong>bold </strong>example text</p>', 'exam'.length);

        const snapped = snapOverflowToWordStart(range, PAGE_RIGHT)!;

        expect(snapped.startContainer).toBe(paragraph);
        expect(snapped.startOffset).toBe(1);
    });

    it('starts before inline elements glued to the word', () => {
        const {range, paragraph} = overflowAt('<p>text <em>un</em><strong>be</strong>lievable more</p>', 'liev'.length);

        const snapped = snapOverflowToWordStart(range, PAGE_RIGHT)!;

        expect(snapped.startContainer).toBe(paragraph);
        expect(snapped.startOffset).toBe(1);
    });
});
