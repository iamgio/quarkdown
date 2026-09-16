import {beforeEach, describe, expect, it} from 'vitest';
import {RestoreCodeIndentation} from "../paged/node/restore-code-indentation";

const SOURCE_TEXT = 'Line 1\n    Line 2\n        Line 3';

describe('RestoreCodeIndentation', () => {
    beforeEach(() => {
        document.body.innerHTML = '';
    });

    function setup(cloneText: string, codeAttributes = 'data-split-from="c1"'): {clone: Text, source: Text} {
        document.body.innerHTML = `<code data-ref="c1">source</code><code ${codeAttributes}>clone</code>`;
        const [sourceCode, splitCode] = Array.from(document.querySelectorAll('code'));
        const source = sourceCode.firstChild as Text;
        source.textContent = SOURCE_TEXT;
        const clone = splitCode.firstChild as Text;
        clone.textContent = cloneText;
        return {clone, source};
    }

    it('accepts text nodes only', () => {
        const handler = new RestoreCodeIndentation();
        expect(handler.accepts(document.createTextNode('text'))).toBe(true);
        expect(handler.accepts(document.createElement('code'))).toBe(false);
    });

    it('restores the indentation of a cut line', () => {
        const {clone, source} = setup('Line 3');

        new RestoreCodeIndentation().render(clone, source);

        expect(clone.textContent).toBe('        Line 3');
    });

    it('leaves uncut text untouched', () => {
        const {clone, source} = setup(SOURCE_TEXT);

        new RestoreCodeIndentation().render(clone, source);

        expect(clone.textContent).toBe(SOURCE_TEXT);
    });

    it('leaves text of unsplit code untouched', () => {
        const {clone, source} = setup('Line 3', 'data-ref="c2"');

        new RestoreCodeIndentation().render(clone, source);

        expect(clone.textContent).toBe('Line 3');
    });

    it('leaves line-numbered code untouched', () => {
        document.body.innerHTML = `
      <code data-ref="c1">${SOURCE_TEXT}</code>
      <code data-split-from="c1"><table class="hljs-ln"><tbody><tr><td class="hljs-ln-code">Line 3</td></tr></tbody></table></code>`;
        const source = document.querySelector('code')!.firstChild as Text;
        const clone = document.querySelector('.hljs-ln-code')!.firstChild as Text;

        new RestoreCodeIndentation().render(clone, source);

        expect(clone.textContent).toBe('Line 3');
    });
});
