import {SplitElement, SplitElementsPaged} from "./split-elements-paged";

/**
 * Document handler that adjusts tables that have been split across page breaks.
 *
 * When a table is split due to page breaks in paged media, the portion carried over
 * to the new page loses the header row, which remains only in the original table.
 *
 * This handler copies the header row of the original table into each split portion,
 * so that every page repeats the header row.
 */
export class SplitTablesPaged extends SplitElementsPaged<HTMLTableElement> {
    protected readonly selector = 'table';

    protected adjust(splitTables: SplitElement<HTMLTableElement>[]) {
        splitTables.forEach(({from, split}) => {
            const header = from.querySelector(':scope > thead');
            if (!header || split.querySelector(':scope > thead')) return;

            split.prepend(header.cloneNode(true));
        });
    }
}
