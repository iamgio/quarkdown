import {beforeEach, describe, expect, it} from 'vitest';
import {CodeCallouts} from "../capabilities/code/callouts";

/**
 * Builds a line-numbered code block as produced by highlight.js
 * with the line numbers plugin.
 */
function lineNumberedCodeBlock(callouts: string | null, lineCount: number): string {
    const lines = Array.from({length: lineCount}, (_, i) => `
        <tr>
            <td class="hljs-ln-line hljs-ln-numbers" data-line-number="${i + 1}"></td>
            <td class="hljs-ln-line hljs-ln-code" data-line-number="${i + 1}">code ${i + 1}</td>
        </tr>`).join('');
    const calloutsAttr = callouts !== null ? ` data-callouts="${callouts}"` : '';
    return `<pre><code class="hljs"${calloutsAttr}><table class="hljs-ln">${lines}</table></code></pre>`;
}

describe('CodeCallouts', () => {
    const codeCallouts = new CodeCallouts();

    beforeEach(() => {
        document.body.innerHTML = '';
    });

    it('appends progressively numbered markers to the marked lines', () => {
        document.body.innerHTML = lineNumberedCodeBlock('1,3', 3);

        codeCallouts.apply();

        const markers = document.querySelectorAll('.code-callout-marker');
        expect(markers.length).toBe(2);

        const line1 = document.querySelector('.hljs-ln-code[data-line-number="1"]')!;
        const line2 = document.querySelector('.hljs-ln-code[data-line-number="2"]')!;
        const line3 = document.querySelector('.hljs-ln-code[data-line-number="3"]')!;

        // The marker is the last child of the marked line.
        expect(line1.lastElementChild?.className).toBe('code-callout-marker');
        expect(line1.lastElementChild?.textContent).toBe('1');
        expect(line2.querySelector('.code-callout-marker')).toBeNull();
        expect(line3.lastElementChild?.className).toBe('code-callout-marker');
        expect(line3.lastElementChild?.textContent).toBe('2');
    });

    it('ignores code blocks without callouts', () => {
        document.body.innerHTML = lineNumberedCodeBlock(null, 2);

        codeCallouts.apply();

        expect(document.querySelector('.code-callout-marker')).toBeNull();
    });

    it('ignores line numbers out of range', () => {
        document.body.innerHTML = lineNumberedCodeBlock('2,99', 2);

        codeCallouts.apply();

        const markers = document.querySelectorAll('.code-callout-marker');
        expect(markers.length).toBe(1);
        // The marker index reflects the callout's position, even if other lines are missing.
        expect(document.querySelector('.hljs-ln-code[data-line-number="2"] .code-callout-marker')?.textContent).toBe('1');
    });

    it('does not fail on code blocks without line numbers', () => {
        document.body.innerHTML = '<pre><code class="hljs" data-callouts="1">plain code</code></pre>';

        expect(() => codeCallouts.apply()).not.toThrow();
        expect(document.querySelector('.code-callout-marker')).toBeNull();
    });

    it('scopes markers to their own code block', () => {
        document.body.innerHTML =
            lineNumberedCodeBlock('2', 2) +
            lineNumberedCodeBlock(null, 2);

        codeCallouts.apply();

        const [withCallouts, withoutCallouts] = document.querySelectorAll('code');
        expect(withCallouts.querySelectorAll('.code-callout-marker').length).toBe(1);
        expect(withoutCallouts.querySelectorAll('.code-callout-marker').length).toBe(0);
    });
});
