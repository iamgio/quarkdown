import type {CodeFeature} from "../code-highlighter";

/**
 * A range of lines to focus, where `NaN` boundaries indicate open ends.
 */
interface FocusRange {
    start: number;
    end: number;
}

/**
 * Applies visual focus to specific line ranges in line-numbered code blocks.
 *
 * Code blocks with the `focus-lines` class have the lines within the range
 * specified by their `data-focus-start` and `data-focus-end` attributes
 * marked with the `focused` class. Open ranges, where either boundary is
 * omitted, are supported:
 * - If start is NaN or missing: focuses from the beginning up to end
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
export class CodeFocus implements CodeFeature {
    apply() {
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
    private extractFocusRange(codeBlock: HTMLElement): FocusRange {
        const start = parseInt(codeBlock.dataset.focusStart ?? '');
        const end = parseInt(codeBlock.dataset.focusEnd ?? '');

        return { start, end };
    }

    /**
     * Applies the 'focused' CSS class to lines within the specified range.
     *
     * @param codeBlock The code block containing the lines to focus
     * @param focusRange Object containing start and end line numbers
     */
    private applyFocusToLines(codeBlock: HTMLElement, focusRange: FocusRange) {
        const lines = codeBlock.querySelectorAll<HTMLElement>('.hljs-ln-line');

        lines.forEach(line => {
            const lineNumber = parseInt(line.dataset.lineNumber || '0');

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
    private isLineInFocusRange(lineNumber: number, focusRange: FocusRange): boolean {
        const { start, end } = focusRange;
        const isAfterStart = isNaN(start) || lineNumber >= start;
        const isBeforeEnd = isNaN(end) || lineNumber <= end;

        return isAfterStart && isBeforeEnd;
    }
}
