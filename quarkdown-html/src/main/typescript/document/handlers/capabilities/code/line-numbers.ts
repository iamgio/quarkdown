import type {CodeFeature} from "../code-highlighter";

/**
 * Type declaration for the highlight.js line numbers plugin.
 */
declare const hljs: {
    lineNumbersBlockSync: (element: Element) => void;
};

/**
 * Adds line numbers to highlighted code blocks via the highlight.js line numbers plugin.
 *
 * Code blocks marked with the `nohljsln` class are excluded.
 * The line structure produced by the plugin (`.hljs-ln-line` elements carrying
 * a `data-line-number` attribute) is what per-line features such as
 * line focusing and callouts rely on.
 */
export class CodeLineNumbers implements CodeFeature {
    apply() {
        document.querySelectorAll('code.hljs:not(.nohljsln)')
            .forEach(codeBlock => hljs.lineNumbersBlockSync(codeBlock));
    }
}
