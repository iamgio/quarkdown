import {PagedNodeHandler} from "./paged-node-handler";

/**
 * Node handler that repeats a split table's header row on each portion of the table.
 */
export class RepeatTableHeaders extends PagedNodeHandler<HTMLTableRowElement | Text> {
    accepts(clone: Node): clone is HTMLTableRowElement | Text {
        return clone instanceof HTMLTableRowElement || clone instanceof Text;
    }

    render(clone: HTMLTableRowElement | Text, source: Node) {
        const table = (clone instanceof HTMLElement ? clone : clone.parentElement)
            ?.closest<HTMLTableElement>('table[data-split-from]');
        if (!table || table.querySelector(':scope > thead')) return;

        const header = (source instanceof HTMLElement ? source : source.parentElement)
            ?.closest('table')
            ?.querySelector(':scope > thead');
        if (header) {
            table.prepend(header.cloneNode(true));
        }
    }
}
