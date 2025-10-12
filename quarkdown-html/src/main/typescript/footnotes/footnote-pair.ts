/**
 * A pair of footnote reference and definition elements.
 */
export interface FootnotePair {
    /** The footnote reference element. */
    readonly reference: HTMLElement;

    /** The footnote definition element. */
    readonly definition: HTMLElement;
}