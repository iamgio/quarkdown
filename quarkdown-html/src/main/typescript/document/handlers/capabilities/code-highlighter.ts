import {DocumentHandler} from "../../document-handler";

/**
 * Type declaration for the highlight.js library with line numbers plugin support.
 */
declare const hljs: typeof import("highlight.js").default & {
    lineNumbersBlockSync: (element: Element) => void;
};

/**
 * Type declaration for the copy button plugin used with highlight.js.
 */
declare const CopyButtonPlugin: { new(...args: any[]): any };

/**
 * Document handler that provides syntax highlighting and code enhancement features.
 *
 * This handler integrates with highlight.js to provide:
 * - Syntax highlighting
 * - Line numbering
 * - Line focusing to emphasize specific code sections
 * - Copy-to-clipboard
 */
export class CodeHighlighter extends DocumentHandler {
    init() {
        hljs.addPlugin({
            'after:highlight': () => window.setTimeout(this.focusCodeLines, 1)
        });
        hljs.addPlugin(new CopyButtonPlugin());
    }

    async onPostRendering() {
        hljs.highlightAll();
        this.initLineNumbers();
    }

    /**
     * Adds line numbers to code blocks with the 'hljs' class, excluding those marked
     * with 'nohljsln' class.
     */
    private initLineNumbers() {
        const codeBlocks = document.querySelectorAll('code.hljs:not(.nohljsln)');
        codeBlocks.forEach((code) => {
            hljs.lineNumbersBlockSync(code);
        });
    }

    /**
     * Applies visual focus to specific line ranges in code blocks.
     *
     * This method processes code blocks with the 'focus-lines' class and highlights
     * lines within the specified range using 'data-focus-start' and 'data-focus-end'
     * attributes. Supports open ranges where either start or end can be omitted.
     *
     * Range behavior:
     * - If start is NaN or missing: focuses from beginning up to end
     * - If end is NaN or missing: focuses from start to the last line
     * - If both are specified: focuses the exact range (inclusive)
     *
     * @example
     * ```html
     * <!-- Focus lines 5-10 -->
     * <code class="focus-lines" data-focus-start="5" data-focus-end="10">...</code>
     *
     * <!-- Focus from line 3 to end -->
     * <code class="focus-lines" data-focus-start="3">...</code>
     *
     * <!-- Focus from beginning to line 8 -->
     * <code class="focus-lines" data-focus-end="8">...</code>
     * ```
     */
    private focusCodeLines() {
        const focusableCodeBlocks = document.querySelectorAll<HTMLElement>('code.focus-lines');

        focusableCodeBlocks.forEach((codeBlock) => {
            const focusRange = this.extractFocusRange(codeBlock);
            this.applyFocusToLines(codeBlock, focusRange);
        });
    }

    /**
     * Extracts the focus range from a code block's data attributes.
     *
     * @param codeBlock The code block element to extract range from
     * @returns An object containing the parsed start and end line numbers
     */
    private extractFocusRange(codeBlock: HTMLElement): { start: number; end: number } {
        const start = parseInt(codeBlock.getAttribute('data-focus-start') || '0');
        const end = parseInt(codeBlock.getAttribute('data-focus-end') || '0');

        return { start, end };
    }

    /**
     * Applies the 'focused' CSS class to lines within the specified range.
     *
     * @param codeBlock The code block containing the lines to focus
     * @param focusRange Object containing start and end line numbers
     */
    private applyFocusToLines(codeBlock: HTMLElement, focusRange: { start: number; end: number }) {
        const lines = codeBlock.querySelectorAll('.hljs-ln-line');

        lines.forEach(line => {
            const lineNumber = parseInt(line.getAttribute('data-line-number') || '0');

            if (this.isLineInFocusRange(lineNumber, focusRange)) {
                line.classList.add('focused');
            }
        });
    }

    /**
     * Determines if a line number falls within the focus range.
     *
     * Supports open ranges where NaN values indicate unbounded ranges:
     * - NaN start means focus from beginning
     * - NaN end means focus to the end
     *
     * @param lineNumber The line number to check
     * @param focusRange The focus range with start and end boundaries
     * @returns True if the line should be focused, false otherwise
     */
    private isLineInFocusRange(lineNumber: number, focusRange: { start: number; end: number }): boolean {
        const { start, end } = focusRange;
        const isAfterStart = isNaN(start) || lineNumber >= start;
        const isBeforeEnd = isNaN(end) || lineNumber <= end;

        return isAfterStart && isBeforeEnd;
    }
}