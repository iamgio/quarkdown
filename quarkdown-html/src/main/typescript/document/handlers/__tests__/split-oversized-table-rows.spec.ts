import {afterEach, expect, it, vi} from 'vitest';
import {SplitOversizedTableRows} from '../paged/node/split-oversized-table-rows';

afterEach(() => {
    document.body.innerHTML = '';
    vi.restoreAllMocks();
});

it.each(['cell', 'text'])('preserves the oversized-row exception in the source for %s clones', kind => {
    const table = '<table><tbody><tr style="break-inside: avoid" data-break-inside="avoid"><td>continued text</td></tr></tbody></table>';
    document.body.innerHTML = `${table}<div class="pagedjs_area">${table}</div>`;
    const [sourceRow, row] = Array.from(document.querySelectorAll('tr'));
    const area = document.querySelector('.pagedjs_area')!;
    vi.spyOn(area, 'getBoundingClientRect').mockReturnValue(new DOMRect(0, 0, 100, 100));
    const rects = [new DOMRect(0, 0, 100, 200)];
    vi.spyOn(row, 'getClientRects').mockReturnValue(Object.assign(rects, {item: (index: number) => rects[index]}));
    const sourceCell = sourceRow.querySelector('td')!;
    const cell = row.querySelector('td')!;
    const source = kind === 'text' ? sourceCell.firstChild! : sourceCell;
    const clone = kind === 'text' ? cell.firstChild! : cell;
    const handler = new SplitOversizedTableRows();

    expect(handler.accepts(clone)).toBe(true);
    if (handler.accepts(clone)) handler.render(clone, source);

    for (const candidate of [sourceRow, row]) {
        expect(candidate.style.breakInside).toBe('auto');
        expect(candidate.style.getPropertyPriority('break-inside')).toBe('important');
        expect(candidate.hasAttribute('data-break-inside')).toBe(false);
    }
});
