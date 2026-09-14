import {beforeEach, describe, expect, it} from 'vitest';
import {SplitTablesPaged} from "../paged/split-tables-paged";
import {QuarkdownDocument} from "../../quarkdown-document";
import {ConditionalDocumentHandler} from "../../document-handler";

class DummyDocument implements QuarkdownDocument {
    getParentViewport(): HTMLElement | undefined {
        return undefined;
    }

    setupPreRenderingHook(): void {
    }

    setupPostRenderingHook(): void {
    }

    initializeRendering(): void {
    }

    getHandlers(): ConditionalDocumentHandler[] {
        return [];
    }
}

const HEADER = '<thead><tr><th>Name</th><th>Value</th></tr></thead>';

describe('SplitTablesPaged', () => {
    beforeEach(() => {
        document.body.innerHTML = '';
    });

    it('copies the header row into a split table', async () => {
        document.body.innerHTML = `
      <table data-ref="t1" data-split-to="t1">
        ${HEADER}
        <tbody><tr><td>A</td><td>1</td></tr></tbody>
      </table>
      <table data-ref="t1" data-split-from="t1">
        <tbody><tr><td>B</td><td>2</td></tr></tbody>
      </table>`;

        await new SplitTablesPaged(new DummyDocument()).onPostRendering();

        const [original, split] = Array.from(document.querySelectorAll('table'));
        expect(original.querySelectorAll('thead').length).toBe(1);
        expect(split.querySelectorAll('thead').length).toBe(1);
        expect(split.firstElementChild?.tagName).toBe('THEAD');
        expect(split.querySelector('thead')?.textContent).toContain('Name');
    });

    it('copies the header row into every portion of a table split across multiple pages', async () => {
        document.body.innerHTML = `
      <table data-ref="t1" data-split-to="t1">
        ${HEADER}
        <tbody><tr><td>A</td><td>1</td></tr></tbody>
      </table>
      <table data-ref="t1" data-split-from="t1" data-split-to="t1">
        <tbody><tr><td>B</td><td>2</td></tr></tbody>
      </table>
      <table data-ref="t1" data-split-from="t1">
        <tbody><tr><td>C</td><td>3</td></tr></tbody>
      </table>`;

        await new SplitTablesPaged(new DummyDocument()).onPostRendering();

        document.querySelectorAll('table').forEach(table => {
            expect(table.querySelectorAll('thead').length).toBe(1);
        });
    });

    it('does not duplicate an existing header row', async () => {
        document.body.innerHTML = `
      <table data-ref="t1" data-split-to="t1">
        ${HEADER}
        <tbody><tr><td>A</td><td>1</td></tr></tbody>
      </table>
      <table data-ref="t1" data-split-from="t1">
        ${HEADER}
        <tbody><tr><td>B</td><td>2</td></tr></tbody>
      </table>`;

        await new SplitTablesPaged(new DummyDocument()).onPostRendering();

        const [, split] = Array.from(document.querySelectorAll('table'));
        expect(split.querySelectorAll('thead').length).toBe(1);
    });

    it('ignores tables that were not split', async () => {
        document.body.innerHTML = `
      <table data-ref="t1">
        ${HEADER}
        <tbody><tr><td>A</td><td>1</td></tr></tbody>
      </table>`;

        await new SplitTablesPaged(new DummyDocument()).onPostRendering();

        expect(document.querySelectorAll('thead').length).toBe(1);
    });

    it('moves a top caption to the first portion', async () => {
        document.body.innerHTML = `
      <table data-ref="t1" data-split-to="t1">
        ${HEADER}
        <tbody><tr><td>A</td><td>1</td></tr></tbody>
      </table>
      <table data-ref="t1" data-split-from="t1">
        <tbody><tr><td>B</td><td>2</td></tr></tbody>
        <caption class="caption-top">My caption</caption>
      </table>`;

        await new SplitTablesPaged(new DummyDocument()).onPostRendering();

        const [original, split] = Array.from(document.querySelectorAll('table'));
        expect(original.querySelectorAll('caption').length).toBe(1);
        expect(split.querySelectorAll('caption').length).toBe(0);
    });

    it('keeps a bottom caption on the last portion', async () => {
        document.body.innerHTML = `
      <table data-ref="t1" data-split-to="t1">
        ${HEADER}
        <tbody><tr><td>A</td><td>1</td></tr></tbody>
      </table>
      <table data-ref="t1" data-split-from="t1">
        <tbody><tr><td>B</td><td>2</td></tr></tbody>
        <caption class="caption-bottom">My caption</caption>
      </table>`;

        await new SplitTablesPaged(new DummyDocument()).onPostRendering();

        const [original, split] = Array.from(document.querySelectorAll('table'));
        expect(original.querySelectorAll('caption').length).toBe(0);
        expect(split.querySelectorAll('caption').length).toBe(1);
    });

    it('ignores split tables whose original has no header row', async () => {
        document.body.innerHTML = `
      <table data-ref="t1" data-split-to="t1">
        <tbody><tr><td>A</td><td>1</td></tr></tbody>
      </table>
      <table data-ref="t1" data-split-from="t1">
        <tbody><tr><td>B</td><td>2</td></tr></tbody>
      </table>`;

        await new SplitTablesPaged(new DummyDocument()).onPostRendering();

        expect(document.querySelectorAll('thead').length).toBe(0);
    });
});
