import {FootnotePair} from "../../../footnotes/footnote-pair";

/** Attribute marking the reference whose page hosts the footnote definition. */
export const FOOTNOTE_ANCHOR_ATTRIBUTE = 'data-footnote-anchor';

/**
 * Footnote definitions taken out of the content flow before pagination,
 * to be placed into page footnote areas while paged.js lays pages out.
 */
export class DetachedFootnotes {
    private readonly definitions = new Map<string, HTMLElement>();

    /**
     * Marks each reference as the anchor of its definition and detaches the definition from the document,
     * so that paged.js neither paginates it as content nor needs space reserved for it in advance.
     * The attribute survives the copy of the document paged.js paginates.
     * @param pairs definitions with their first reference
     */
    detach(pairs: FootnotePair[]) {
        pairs.forEach(({reference, definition}) => {
            reference.setAttribute(FOOTNOTE_ANCHOR_ATTRIBUTE, '');
            definition.remove();
            this.definitions.set(definition.id, definition);
        });
    }

    /**
     * @param anchor a reference marked by {@link detach}, or a clone of it
     * @returns the detached definition the anchor refers to, if any
     */
    definitionOf(anchor: Element): HTMLElement | undefined {
        const id = anchor.getAttribute('data-definition');
        return id ? this.definitions.get(id) : undefined;
    }
}
