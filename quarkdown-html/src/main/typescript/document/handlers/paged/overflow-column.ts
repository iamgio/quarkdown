/**
 * paged.js lays the content of a page out in a multi-column box: content that does not fit the page flows into
 * a hidden column far to the right, from which it is carried over to the next page.
 * @param rect box of some rendered content
 * @param pageRight right edge of the page content box
 * @returns whether the box lies in the hidden overflow column, hence is not on the page
 */
export function isInOverflowColumn(rect: DOMRect, pageRight: number): boolean {
    return rect.left >= pageRight;
}
