import {beforeEach, describe, expect, it} from 'vitest';
import {CodeFocus} from "../capabilities/code/focus";

/**
 * Builds a focusable code block with the given focus attributes
 * and the line structure produced by the line numbers plugin.
 */
function focusableCodeBlock(focusStart: number | null, focusEnd: number | null, lineCount: number): string {
    const lines = Array.from({length: lineCount}, (_, i) =>
        `<div class="hljs-ln-line" data-line-number="${i + 1}"></div>`).join('');
    const attributes = [
        focusStart !== null ? `data-focus-start="${focusStart}"` : '',
        focusEnd !== null ? `data-focus-end="${focusEnd}"` : '',
    ].join(' ');
    return `<pre><code class="focus-lines" ${attributes}>${lines}</code></pre>`;
}

function focusedLineNumbers(): number[] {
    return [...document.querySelectorAll<HTMLElement>('.hljs-ln-line.focused')]
        .map(line => parseInt(line.dataset.lineNumber!));
}

describe('CodeFocus', () => {
    const codeFocus = new CodeFocus();

    beforeEach(() => {
        document.body.innerHTML = '';
    });

    it('focuses a closed range', () => {
        document.body.innerHTML = focusableCodeBlock(2, 3, 4);

        codeFocus.apply();

        expect(focusedLineNumbers()).toEqual([2, 3]);
    });

    it('focuses an open-ended range from start to the last line', () => {
        document.body.innerHTML = focusableCodeBlock(3, null, 4);

        codeFocus.apply();

        expect(focusedLineNumbers()).toEqual([3, 4]);
    });

    it('focuses an open-ended range from the beginning up to end', () => {
        document.body.innerHTML = focusableCodeBlock(null, 2, 4);

        codeFocus.apply();

        expect(focusedLineNumbers()).toEqual([1, 2]);
    });
});
