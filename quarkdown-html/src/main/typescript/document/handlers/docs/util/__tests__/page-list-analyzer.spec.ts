import {beforeEach, describe, expect, it} from 'vitest';
import {PageListAnalyzer} from "../page-list-analyzer";

describe('PageListAnalyzer', () => {
    beforeEach(() => {
        document.body.innerHTML = '';
    });

    it('returns null when nav does not exist', () => {
        const analyzer = new PageListAnalyzer();
        expect(analyzer.getNextPageLink()).toBeNull();
        expect(analyzer.getPreviousPageLink()).toBeNull();
    });

    it('returns null when there is no current page', () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a">A</a></li>
                    <li><a href="/b">B</a></li>
                </ol>
            </nav>`;

        const analyzer = new PageListAnalyzer();
        expect(analyzer.getNextPageLink()).toBeNull();
        expect(analyzer.getPreviousPageLink()).toBeNull();
    });

    it('returns null for previous when current is first', () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a" aria-current="page">A</a></li>
                    <li><a href="/b">B</a></li>
                </ol>
            </nav>`;

        const analyzer = new PageListAnalyzer();
        expect(analyzer.getPreviousPageLink()).toBeNull();
        expect(analyzer.getNextPageLink()?.textContent).toBe('B');
    });

    it('returns null for next when current is last', () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a">A</a></li>
                    <li><a href="/b" aria-current="page">B</a></li>
                </ol>
            </nav>`;

        const analyzer = new PageListAnalyzer();
        expect(analyzer.getPreviousPageLink()?.textContent).toBe('A');
        expect(analyzer.getNextPageLink()).toBeNull();
    });

    it('returns previous and next links in flat list', () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li><a href="/a">A</a></li>
                    <li><a href="/b" aria-current="page">B</a></li>
                    <li><a href="/c">C</a></li>
                </ol>
            </nav>`;

        const analyzer = new PageListAnalyzer();
        expect(analyzer.getPreviousPageLink()?.textContent).toBe('A');
        expect(analyzer.getNextPageLink()?.textContent).toBe('C');
    });

    it('handles nested lists - previous in parent list', () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li>
                        <a href="/a">A</a>
                        <ol>
                            <li><a href="/b" aria-current="page">B</a></li>
                        </ol>
                    </li>
                    <li><a href="/c">C</a></li>
                </ol>
            </nav>`;

        const analyzer = new PageListAnalyzer();
        expect(analyzer.getPreviousPageLink()?.textContent).toBe('A');
        expect(analyzer.getNextPageLink()?.textContent).toBe('C');
    });

    it('handles nested lists - next in child list', () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li>
                        <a href="/a" aria-current="page">A</a>
                        <ol>
                            <li><a href="/b">B</a></li>
                        </ol>
                    </li>
                    <li><a href="/c">C</a></li>
                </ol>
            </nav>`;

        const analyzer = new PageListAnalyzer();
        expect(analyzer.getPreviousPageLink()).toBeNull();
        expect(analyzer.getNextPageLink()?.textContent).toBe('B');
    });

    it('handles deeply nested lists', () => {
        document.body.innerHTML = `
            <nav data-role="page-list">
                <ol>
                    <li>
                        <a href="/a">A</a>
                        <ol>
                            <li><a href="/b">B</a></li>
                        </ol>
                    </li>
                    <li><a href="/c" aria-current="page">C</a></li>
                    <li><a href="/d">D</a></li>
                </ol>
            </nav>`;

        const analyzer = new PageListAnalyzer();
        expect(analyzer.getPreviousPageLink()?.textContent).toBe('B');
        expect(analyzer.getNextPageLink()?.textContent).toBe('D');
    });
});
