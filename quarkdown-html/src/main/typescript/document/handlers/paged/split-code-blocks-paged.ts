import {SplitElement, SplitElementsPaged} from "./split-elements-paged";

/**
 * Document handler that adjusts code blocks that have been split across page breaks.
 *
 * Code blocks are highlighted before pagination, so blocks with line numbers are
 * backed by a table that paged.js splits at row boundaries, keeping lines intact
 * and line numbers continuous, with no adjustment needed.
 *
 * Blocks without line numbers, however, are split in the middle of their raw text:
 * the split portion loses the indentation of its first line, which this handler restores.
 */
export class SplitCodeBlocksPaged extends SplitElementsPaged {
    protected readonly selector = 'code';

    /**
     * Fixes the indentation of the first line in split code blocks.
     *
     * When a code block without line numbers is split, the first line of the split
     * portion loses its indentation, which paged.js leaves in the last line of the
     * original block. This method extracts it from there and applies it to the split block.
     *
     * @param splitCodeBlocks Array of split code block pairs to adjust
     */
    private fixFirstLineIndentation(splitCodeBlocks: SplitElement[]) {
        splitCodeBlocks.forEach(({from, split}) => {
            // Line-numbered blocks are split at line boundaries, keeping indentation intact.
            if (split.querySelector('table.hljs-ln')) return;

            // The indentation of the first line is contained in the last line of the original code block.
            const fromLastLine = from.innerText.split('\n').pop();
            if (!fromLastLine) return;
            const indentation = fromLastLine.match(/\s*$/)?.[0] || '';

            split.innerHTML = indentation + split.innerHTML;
        })
    }

    protected adjust(splitCodeBlocks: SplitElement[]) {
        this.fixFirstLineIndentation(splitCodeBlocks);
    }
}
