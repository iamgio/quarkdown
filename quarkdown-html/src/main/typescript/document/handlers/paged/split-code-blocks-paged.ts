import {SplitElement, SplitElementsPaged} from "./split-elements-paged";

/**
 * Document handler that adjusts code blocks that have been split across page breaks.
 *
 * When code blocks are split due to page breaks in paged media, several issues can occur:
 * - The split code block loses proper indentation on its first line
 * - Line numbers restart from 1 instead of continuing from the original block
 *
 * This handler corrects these formatting issues.
 */
export class SplitCodeBlocksPaged extends SplitElementsPaged {
    protected readonly selector = 'code';

    /**
     * Fixes the indentation of the first line in split code blocks.
     *
     * When a code block is split, the first line of the split portion often loses
     * its proper indentation. This method extracts the indentation from the last
     * line of the original code block and applies it to the split block.
     *
     * @param splitCodeBlocks Array of split code block pairs to adjust
     */
    private fixFirstLineIndentation(splitCodeBlocks: SplitElement[]) {
        splitCodeBlocks.forEach(({from, split}) => {
            // The indentation of the first line is contained in the last line of the original code block.
            const fromLastLine = from.innerText.split('\n').pop();
            if (!fromLastLine) return;
            const indentation = fromLastLine.match(/\s*$/)?.[0] || '';

            split.innerHTML = indentation + split.innerHTML;
        })
    }

    /**
     * Corrects line numbers in split code blocks to continue from the original block.
     *
     * Split code blocks typically restart their line numbering from 1, but they should
     * continue the numbering sequence from where the original block left off. This method
     * finds the last line number in the original block and adjusts all line numbers
     * in the split block accordingly.
     *
     * @param splitCodeBlocks Array of split code block pairs to adjust
     */
    private fixLineNumbers(splitCodeBlocks: SplitElement[]) {
        const lineNumberAttribute = 'data-line-number';

        splitCodeBlocks.forEach(({from, split}) => {
            const lines = from.querySelectorAll(`[${lineNumberAttribute}]`);
            const lastLineNumber = Array.from(lines).pop()?.getAttribute(lineNumberAttribute) || '0';

            split.querySelectorAll(`[${lineNumberAttribute}]`).forEach(line => {
                const lineNumber = line.getAttribute(lineNumberAttribute);
                if (!lineNumber) return;
                line.setAttribute(lineNumberAttribute, (parseInt(lineNumber) + parseInt(lastLineNumber)).toString());
            });
        });
    }

    protected adjust(splitCodeBlocks: SplitElement[]) {
        this.fixFirstLineIndentation(splitCodeBlocks);
        setTimeout(() => this.fixLineNumbers(splitCodeBlocks), 0); // Must execute after the highlighting is done.
    }
}
