import {PagedNodeHandler} from "./paged-node-handler";

/**
 * Node handler that allows an otherwise unbreakable table row to split when
 * it cannot fit on a page, including the space occupied by its repeated header.
 */
export class SplitOversizedTableRows extends PagedNodeHandler<HTMLTableCellElement | Text> {
    accepts(clone: Node): clone is HTMLTableCellElement | Text {
        return clone instanceof HTMLTableCellElement || clone instanceof Text;
    }

    render(clone: HTMLTableCellElement | Text, source: Node) {
        const element = clone instanceof HTMLElement ? clone : clone.parentElement;
        const row = element?.closest<HTMLTableRowElement>('tbody > tr');
        const area = element?.closest<HTMLElement>('.pagedjs_area');
        if (!row || !area || getComputedStyle(row).breakInside !== 'avoid') return;

        const header = row.closest('table')?.tHead;
        const availableHeight = area.getBoundingClientRect().height - (header?.getBoundingClientRect().height ?? 0);
        // The browser may already fragment the row across overflow columns,
        // so its bounding box alone does not expose its full height.
        const height = Array.from(row.getClientRects()).reduce((sum, rect) => sum + rect.height, 0);
        if (height > availableHeight) {
            const sourceRow = (source instanceof HTMLElement ? source : source.parentElement)
                ?.closest<HTMLTableRowElement>('tbody > tr');
            // Continuations are cloned from the source, including its break metadata.
            for (const candidate of [row, sourceRow]) {
                if (!candidate) continue;
                candidate.style.setProperty('break-inside', 'auto', 'important');
                delete candidate.dataset.breakInside;
            }
        }
    }
}
