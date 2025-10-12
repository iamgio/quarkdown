import {describe, expect, it, vi} from 'vitest';
import {CodeHighlighter} from "../capabilities/code-highlighter";

// mock hljs and plugin
// @ts-expect-error define global
globalThis.CopyButtonPlugin = function(){} as any;
// mock hljs
const highlightAll = vi.fn();
const lineNumbersBlockSync = vi.fn();
const addPlugin = vi.fn();

// @ts-expect-error define global hljs
globalThis.hljs = { highlightAll, lineNumbersBlockSync, addPlugin } as any;

class DummyDoc {}

describe('CodeHighlighter', () => {
  it('initializes plugin, highlights, adds line numbers and focuses lines', async () => {
    document.body.innerHTML = `
      <pre><code class="hljs">code</code></pre>
      <pre><code class="focus-lines" data-focus-start="2" data-focus-end="3">
        <div class="hljs-ln-line" data-line-number="1"></div>
        <div class="hljs-ln-line" data-line-number="2"></div>
        <div class="hljs-ln-line" data-line-number="3"></div>
        <div class="hljs-ln-line" data-line-number="4"></div>
      </code></pre>`;

    const h = new CodeHighlighter(new DummyDoc() as any);
    h.init();
    expect(addPlugin).toHaveBeenCalled();

    await h.onPostRendering();

    expect(highlightAll).toHaveBeenCalled();
    expect(lineNumbersBlockSync).toHaveBeenCalled();

    const focused = document.querySelectorAll('.hljs-ln-line.focused');
    expect(focused.length).toBe(2); // lines 2 and 3
  });
});
