import {PagedNodeHandler} from "./paged-node-handler";

/**
 * Node handler that repeats a split table's header row on each portion of the table.
 */
export class RepeatTableHeaders extends PagedNodeHandler<HTMLTableRowElement> {
    accepts(clone: Node): clone is HTMLTableRowElement {
        return clone instanceof HTMLTableRowElement;
    }

    render(clone: HTMLTableRowElement, source: Node) {
        const table = clone.closest<HTMLTableElement>('table[data-split-from]');
        if (!table || table.querySelector(':scope > thead')) return;

        const header = (source instanceof HTMLElement ? source : source.parentElement)
            ?.closest('table')
            ?.querySelector(':scope > thead');
        if (header) {
            table.prepend(header.cloneNode(true));
        }
    }
}
