import {beforeEach, describe, expect, it, vi} from 'vitest';
import {CodeLineNumbers} from "../capabilities/code/line-numbers";

const lineNumbersBlockSync = vi.fn();

// @ts-expect-error define global hljs
globalThis.hljs = {lineNumbersBlockSync} as any;

describe('CodeLineNumbers', () => {
    beforeEach(() => {
        document.body.innerHTML = '';
        lineNumbersBlockSync.mockClear();
    });

    it('numbers the lines of code blocks', () => {
        document.body.innerHTML = '<pre><code class="hljs">line</code></pre>';

        new CodeLineNumbers().apply();

        expect(lineNumbersBlockSync).toHaveBeenCalledTimes(1);
    });

    it('skips empty code blocks, which have no lines to number', () => {
        document.body.innerHTML = '<pre><code class="hljs"></code></pre>';

        new CodeLineNumbers().apply();

        expect(lineNumbersBlockSync).not.toHaveBeenCalled();
    });

    it('numbers code blocks of whitespace, which do have lines', () => {
        document.body.innerHTML = '<pre><code class="hljs"> </code></pre>';

        new CodeLineNumbers().apply();

        expect(lineNumbersBlockSync).toHaveBeenCalledTimes(1);
    });

    it('skips code blocks that opted out of line numbers', () => {
        document.body.innerHTML = '<pre><code class="hljs nohljsln">line</code></pre>';

        new CodeLineNumbers().apply();

        expect(lineNumbersBlockSync).not.toHaveBeenCalled();
    });
});
