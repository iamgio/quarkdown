import {PagedNodeHandler} from "./paged-node-handler";

/**
 * Node handler that allows an otherwise unbreakable table row to split when
 * it cannot fit on a page, including the space occupied by its repeated header.
 */
export class SplitOversizedTableRows extends PagedNodeHandler<HTMLTableCellElement> {
    accepts(clone: Node): clone is HTMLTableCellElement {
        return clone instanceof HTMLTableCellElement;
    }

    render(clone: HTMLTableCellElement) {
        const row = clone.closest<HTMLTableRowElement>('tbody > tr');
        const area = clone.closest<HTMLElement>('.pagedjs_area');
        if (!row || !area || getComputedStyle(row).breakInside !== 'avoid') return;

        const header = row.closest('table')?.tHead;
        const availableHeight = area.getBoundingClientRect().height - (header?.getBoundingClientRect().height ?? 0);
        // The browser may already fragment the row across overflow columns,
        // so its bounding box alone does not expose its full height.
        const height = Array.from(row.getClientRects()).reduce((sum, rect) => sum + rect.height, 0);
        if (height > availableHeight) {
            row.style.setProperty('break-inside', 'auto', 'important');
            delete row.dataset.breakInside;
        }
    }
}
