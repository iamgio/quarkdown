import {DocumentHandler} from "../../document-handler";

/**
 * Represents a pair of code blocks where one was split from the other due to page breaks.
 */
interface SplitCodeBlock {
    /** The original code block that was split */
    from: HTMLElement;
    /** The new code block created from the split */
    split: HTMLElement;
}

/**
 * Document handler that fixes issues with code blocks that have been split across page breaks.
 *
 * When code blocks are split due to page breaks in paged media, several issues can occur:
 * - The split code block loses proper indentation on its first line
 * - Line numbers restart from 1 instead of continuing from the original block
 *
 * This handler identifies split code blocks using `data-split-from` and `data-ref` attributes
 * and corrects these formatting issues.
 */
export class SplitCodeBlocksFixPaged extends DocumentHandler {
    /**
     * Identifies and returns all code blocks that were split due to page breaks.
     *
     * Split code blocks are identified by the presence of a `data-split-from` attribute,
     * which contains the `data-ref` value of the original code block they were split from.
     *
     * @returns An array of split code block pairs, each containing the original block and its split counterpart
     */
    private getSplitCodeBlocks(): SplitCodeBlock[] {
        const splitCodeBlocks: SplitCodeBlock[] = [];

        // Splits code blocks have the attribute `data-split-from`, where its value
        // is the `data-ref` attribute of the code block it was split from.
        document.querySelectorAll<HTMLElement>('code[data-split-from]').forEach(split => {
            const fromRef = split.getAttribute('data-split-from');
            if (!fromRef) return splitCodeBlocks;

            const from = document.querySelector<HTMLElement>(`code[data-ref="${fromRef}"]`);
            if (!from) return splitCodeBlocks;

            splitCodeBlocks.push({from, split});
        });

        return splitCodeBlocks;
    }

    /**
     * Fixes the indentation of the first line in split code blocks.
     *
     * When a code block is split, the first line of the split portion often loses
     * its proper indentation. This method extracts the indentation from the last
     * line of the original code block and applies it to the split block.
     *
     * @param splitCodeBlocks Array of split code block pairs to fix
     */
    private fixSplitCodeBlockFirstLineIndentation(splitCodeBlocks: SplitCodeBlock[]) {
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
     * @param splitCodeBlocks Array of split code block pairs to fix
     */
    private fixSplitCodeBlockLineNumbers(splitCodeBlocks: SplitCodeBlock[]) {
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

    /**
     * Executes the split code block fixes after document rendering is complete.
     *
     * This method is called during the post-rendering phase and:
     * 1. Identifies all split code blocks in the document
     * 2. Fixes their line numbering immediately
     * 3. Schedules another line number fix after syntax highlighting completes
     *
     * The setTimeout is necessary because syntax highlighting may modify the DOM
     * after initial rendering, potentially affecting line number attributes.
     */
    async onPostRendering() {
        const splitCodeBlocks = this.getSplitCodeBlocks();
        this.fixSplitCodeBlockFirstLineIndentation(splitCodeBlocks);
        setTimeout(() => this.fixSplitCodeBlockLineNumbers(splitCodeBlocks), 0); // Must execute after the highlighting is done.
    }
}