import {isInOverflowColumn} from "./overflow-column";

/** Characters paged.js treats as word separators when scanning text for a page break. */
const SEPARATOR = /[^\S\u202F\u00A0]/;

/**
 * Snaps an overflow range that starts inside a word back to the start of that word.
 *
 * paged.js scans overflowing text letter by letter. On a line the browser hyphenated, the first letter of the
 * continuation reports a bounding box that includes the hyphen on the previous line, so paged.js considers it
 * still fitting and breaks the page one letter too late. The leftover letter and the hyphen glyph paged.js appends
 * no longer fit on the line they were hyphenated off of, and the reflow pushes whole lines off the page.
 * Breaking before the whole word leaves the laid-out page untouched.
 *
 * A word is snapped when the part of it that fits on the page sits on a single line, which is what a word
 * hyphenated off the last line looks like. A run without separators that already spans several lines
 * (a paragraph in a script without inter-word spaces, a wrapped URL) is broken by paged.js as usual.
 *
 * @param overflow range from the first overflowing content to the end of the page
 * @param pageRight right edge of the page content box, beyond which paged.js lays overflowing content out
 * @returns the adjusted range, or `undefined` if the range does not start inside a snappable word
 */
export function snapOverflowToWordStart(overflow: Range, pageRight: number): Range | undefined {
    const container = overflow.startContainer;
    if (!(container instanceof Text)) return undefined;

    const text = container.data;
    const offset = overflow.startOffset;
    const insideWord = offset > 0 && offset < text.length
        && !SEPARATOR.test(text[offset - 1]) && !SEPARATOR.test(text[offset]);
    if (!insideWord) return undefined;

    let wordStart = offset;
    while (wordStart > 0 && !SEPARATOR.test(text[wordStart - 1])) wordStart--;
    if (!fitsOnOneLine(container, wordStart, offset, pageRight)) return undefined;

    const snapped = overflow.cloneRange();
    if (wordStart === 0) {
        snapped.setStartBefore(firstGluedNode(container));
    } else {
        snapped.setStart(container, wordStart);
    }
    return snapped;
}

/**
 * @returns whether the text between the two offsets is laid out on a single line of the page
 */
function fitsOnOneLine(container: Text, from: number, to: number, pageRight: number): boolean {
    const fitted = document.createRange();
    fitted.setStart(container, from);
    fitted.setEnd(container, to);
    const lineTops = new Set(
        Array.from(fitted.getClientRects())
            .filter(rect => rect.width > 0 && !isInOverflowColumn(rect, pageRight))
            .map(rect => Math.round(rect.top)),
    );
    return lineTops.size === 1;
}

/**
 * Walks back over the inline siblings glued to the start of the given text node.
 * @returns the first node of the word the text node starts
 */
function firstGluedNode(container: Text): Node {
    let first: Node = container;
    while (first.previousSibling && isGluedToNext(first.previousSibling)) {
        first = first.previousSibling;
    }
    return first;
}

function isGluedToNext(node: Node): boolean {
    const text = node.textContent ?? '';
    return text.length > 0 && !SEPARATOR.test(text[text.length - 1]);
}
