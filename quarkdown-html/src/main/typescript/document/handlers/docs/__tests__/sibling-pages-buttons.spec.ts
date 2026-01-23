import {beforeEach, describe, expect, it} from 'vitest';
import {SiblingPagesButtons} from "../sibling-pages-buttons";

class DummyDoc {
}

describe('SiblingPagesButtons', () => {
    beforeEach(() => {
        document.body.innerHTML = '';
    });

    it('does nothing when button area does not exist', async () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a">A</a></li>
                    <li><a href="/b" aria-current="page">B</a></li>
                    <li><a href="/c">C</a></li>
                </ol>
            </nav>`;

        const handler = new SiblingPagesButtons(new DummyDoc() as any);
        await handler.onPostRendering();

        expect(document.getElementById('previous-page-anchor')).toBeNull();
        expect(document.getElementById('next-page-anchor')).toBeNull();
    });

    it('creates both links when previous and next exist', async () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a">A</a></li>
                    <li><a href="/b" aria-current="page">B</a></li>
                    <li><a href="/c">C</a></li>
                </ol>
            </nav>
            <div id="sibling-pages-button-area"></div>`;

        const handler = new SiblingPagesButtons(new DummyDoc() as any);
        await handler.onPostRendering();

        const prevLink = document.getElementById('previous-page-anchor') as HTMLAnchorElement;
        const nextLink = document.getElementById('next-page-anchor') as HTMLAnchorElement;

        expect(prevLink).not.toBeNull();
        expect(nextLink).not.toBeNull();

        expect(prevLink.getAttribute('href')).toBe('/a');
        expect(nextLink.getAttribute('href')).toBe('/c');
    });

    it('places icon at start for previous link', async () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a">A</a></li>
                    <li><a href="/b" aria-current="page">B</a></li>
                </ol>
            </nav>
            <div id="sibling-pages-button-area"></div>`;

        const handler = new SiblingPagesButtons(new DummyDoc() as any);
        await handler.onPostRendering();

        const prevLink = document.getElementById('previous-page-anchor') as HTMLAnchorElement;
        const icon = prevLink.querySelector('i') as HTMLElement;

        expect(icon.className).toBe('bi bi-arrow-left');
        expect(prevLink.firstChild).toBe(icon);
    });

    it('places icon at end for next link', async () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a" aria-current="page">A</a></li>
                    <li><a href="/b">B</a></li>
                </ol>
            </nav>
            <div id="sibling-pages-button-area"></div>`;

        const handler = new SiblingPagesButtons(new DummyDoc() as any);
        await handler.onPostRendering();

        const nextLink = document.getElementById('next-page-anchor') as HTMLAnchorElement;
        const icon = nextLink.querySelector('i') as HTMLElement;

        expect(icon.className).toBe('bi bi-arrow-right');
        expect(nextLink.lastChild).toBe(icon);
    });

    it('only creates previous link when at last page', async () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a">A</a></li>
                    <li><a href="/b" aria-current="page">B</a></li>
                </ol>
            </nav>
            <div id="sibling-pages-button-area"></div>`;

        const handler = new SiblingPagesButtons(new DummyDoc() as any);
        await handler.onPostRendering();

        expect(document.getElementById('previous-page-anchor')).not.toBeNull();
        expect(document.getElementById('next-page-anchor')).toBeNull();
    });

    it('only creates next link when at first page', async () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a" aria-current="page">A</a></li>
                    <li><a href="/b">B</a></li>
                </ol>
            </nav>
            <div id="sibling-pages-button-area"></div>`;

        const handler = new SiblingPagesButtons(new DummyDoc() as any);
        await handler.onPostRendering();

        expect(document.getElementById('previous-page-anchor')).toBeNull();
        expect(document.getElementById('next-page-anchor')).not.toBeNull();
    });

    it('clones the link without modifying the original', async () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a" aria-current="page">A</a></li>
                    <li><a href="/b">B</a></li>
                </ol>
            </nav>
            <div id="sibling-pages-button-area"></div>`;

        const originalLink = document.querySelector('a[href="/b"]') as HTMLAnchorElement;
        const originalChildCount = originalLink.childNodes.length;

        const handler = new SiblingPagesButtons(new DummyDoc() as any);
        await handler.onPostRendering();

        expect(originalLink.childNodes.length).toBe(originalChildCount);
        expect(originalLink.querySelector('i')).toBeNull();
    });
});
