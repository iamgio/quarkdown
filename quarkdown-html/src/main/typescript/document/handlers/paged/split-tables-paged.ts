import {SplitElement, SplitElementsPaged} from "./split-elements-paged";

/**
 * Document handler that adjusts tables that have been split across page breaks.
 *
 * When a table is split due to page breaks in paged media:
 * - The portion carried over to the new page loses the header row,
 *   which remains only in the original table.
 * - The caption, which follows the table content in the DOM, is carried over
 *   to the last portion regardless of the side it is displayed on.
 *
 * This handler copies the header row of the original table into each split portion,
 * so that every page repeats the header row, and moves the caption to the portion
 * matching its position: a top caption to the first portion, a bottom caption to the last.
 */
export class SplitTablesPaged extends SplitElementsPaged<HTMLTableElement> {
    protected readonly selector = 'table';

    /**
     * Copies the header row of the original table into its split counterparts
     * that lack one, so that every page repeats the header row.
     *
     * @param splitTables Array of split table pairs to adjust
     */
    private repeatHeaders(splitTables: SplitElement<HTMLTableElement>[]) {
        splitTables.forEach(({from, split}) => {
            const header = from.querySelector(':scope > thead');
            if (!header || split.querySelector(':scope > thead')) return;

            split.prepend(header.cloneNode(true));
        });
    }

    /**
     * Moves the caption of each split table to the portion matching its displayed side:
     * a top caption belongs to the first portion, a bottom caption to the last one.
     *
     * @param splitTables Array of split table pairs to adjust
     */
    private repositionCaptions(splitTables: SplitElement<HTMLTableElement>[]) {
        // All portions of each table, in document order: the original first, then its splits.
        const portions = new Map<HTMLTableElement, HTMLTableElement[]>();
        splitTables.forEach(({from, split}) => {
            if (!portions.has(from)) portions.set(from, [from]);
            portions.get(from)!.push(split);
        });

        portions.forEach(parts => {
            const caption = parts
                .map(part => part.querySelector<HTMLElement>(':scope > caption'))
                .find(Boolean);
            if (!caption) return;

            const isBottom = getComputedStyle(caption).captionSide === 'bottom';
            const target = isBottom ? parts[parts.length - 1] : parts[0];
            if (caption.parentElement !== target) {
                target.prepend(caption);
            }
        });
    }

    protected adjust(splitTables: SplitElement<HTMLTableElement>[]) {
        this.repeatHeaders(splitTables);
        this.repositionCaptions(splitTables);
    }
}
