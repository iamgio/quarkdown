import {PagedNodeHandler} from "./paged-node-handler";

/**
 * Node handler that restores the indentation of the first line of a code block
 * portion carried over to a new page.
 */
export class RestoreCodeIndentation extends PagedNodeHandler<Text> {
    accepts(clone: Node): clone is Text {
        return clone instanceof Text;
    }

    render(clone: Text, source: Node) {
        const cloneText = clone.textContent ?? '';
        const sourceText = source.textContent ?? '';

        // Only the text node cut by the page break loses its indentation,
        // with its clone holding the trailing part of the source text.
        if (cloneText === sourceText || !sourceText.endsWith(cloneText)) return;

        const code = clone.parentElement?.closest('code[data-split-from]');
        // Line-numbered blocks are split at line boundaries, keeping indentation intact.
        if (!code || clone.parentElement?.closest('.hljs-ln')) return;

        // The indentation of the cut line is left at the end of the previous portion.
        const cutPoint = sourceText.length - cloneText.length;
        const lastLineBeforeCut = sourceText.slice(0, cutPoint).split('\n').pop() ?? '';
        const indentation = lastLineBeforeCut.match(/\s*$/)?.[0] ?? '';

        clone.textContent = indentation + cloneText;
    }
}
