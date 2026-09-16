import {beforeEach, describe, expect, it} from 'vitest';
import {RepeatTableHeaders} from "../paged/node/repeat-table-headers";

const HEADER = '<thead><tr><th>Name</th></tr></thead>';

describe('RepeatTableHeaders', () => {
    beforeEach(() => {
        document.body.innerHTML = '';
    });

    function setup(splitTableHtml: string): {clone: HTMLElement, source: HTMLElement} {
        document.body.innerHTML = `
      <div id="source">
        <table data-ref="t1">
          ${HEADER}
          <tbody><tr id="source-row"><td>A</td></tr></tbody>
        </table>
      </div>
      <div id="page">${splitTableHtml}</div>`;
        return {
            clone: document.querySelector('#page tr')!,
            source: document.querySelector('#source-row')!,
        };
    }

    it('clones the header into a split table when its first row renders', () => {
        const {clone, source} = setup(`
      <table data-ref="t1" data-split-from="t1">
        <tbody><tr><td>B</td></tr></tbody>
      </table>`);

        new RepeatTableHeaders().render(clone, source);

        const table = document.querySelector('#page table')!;
        expect(table.querySelectorAll(':scope > thead').length).toBe(1);
        expect(table.firstElementChild?.tagName).toBe('THEAD');
    });

    it('does not duplicate an existing header', () => {
        const {clone, source} = setup(`
      <table data-ref="t1" data-split-from="t1">
        ${HEADER}
        <tbody><tr><td>B</td></tr></tbody>
      </table>`);

        new RepeatTableHeaders().render(clone, source);

        expect(document.querySelectorAll('#page thead').length).toBe(1);
    });

    it('ignores rows of tables that were not split', () => {
        const {clone, source} = setup(`
      <table data-ref="t2">
        <tbody><tr><td>B</td></tr></tbody>
      </table>`);

        new RepeatTableHeaders().render(clone, source);

        expect(document.querySelectorAll('#page thead').length).toBe(0);
    });

    it('ignores split tables whose source has no header', () => {
        document.body.innerHTML = `
      <div id="source">
        <table data-ref="t1">
          <tbody><tr id="source-row"><td>A</td></tr></tbody>
        </table>
      </div>
      <div id="page">
        <table data-ref="t1" data-split-from="t1">
          <tbody><tr><td>B</td></tr></tbody>
        </table>
      </div>`;

        new RepeatTableHeaders().render(document.querySelector('#page tr')!, document.querySelector('#source-row')!);

        expect(document.querySelectorAll('#page thead').length).toBe(0);
    });

    it('accepts table rows only', () => {
        const {clone} = setup(`
      <table data-ref="t1" data-split-from="t1">
        <tbody><tr><td>B</td></tr></tbody>
      </table>`);

        const handler = new RepeatTableHeaders();
        expect(handler.accepts(clone)).toBe(true);
        expect(handler.accepts(document.querySelector('#page td')!)).toBe(false);
    });
});
