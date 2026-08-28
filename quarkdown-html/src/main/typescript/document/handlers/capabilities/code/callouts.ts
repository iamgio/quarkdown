import type {CodeFeature} from "../code-highlighter";

/**
 * Injects callout markers into line-numbered code blocks.
 *
 * A code block carrying a `data-callouts` attribute, a comma-separated list of
 * 1-based line numbers (e.g. `data-callouts="1,3"`), gets a numbered marker
 * appended at the end of each referenced line. Markers are numbered
 * progressively (1, 2, ...) following the order of the attribute, matching the
 * numbering of the callout description list rendered below the code block.
 */
export class CodeCallouts implements CodeFeature {
    /** CSS class of the injected marker elements. */
    private static readonly MARKER_CLASS = 'code-callout-marker';

    /**
     * Injects callout markers into all code blocks that declare callouts.
     */
    apply() {
        document.querySelectorAll<HTMLElement>('code[data-callouts]')
            .forEach(codeBlock => this.applyToBlock(codeBlock));
    }

    /**
     * Injects the markers declared by a single code block into its lines.
     * Lines that do not exist (e.g. an out-of-range line number, or a block
     * rendered without line numbers) are skipped, without affecting the
     * numbering of the other markers.
     * @param codeBlock the line-numbered code block to inject markers into
     */
    private applyToBlock(codeBlock: HTMLElement) {
        this.extractCalloutLines(codeBlock).forEach((lineNumber, index) => {
            codeBlock
                .querySelector(`.hljs-ln-code[data-line-number="${lineNumber}"]`)
                ?.appendChild(this.createMarker(index + 1));
        });
    }

    /**
     * Parses the line numbers declared in a code block's `data-callouts` attribute.
     *
     * @param codeBlock the code block to extract callout lines from
     * @returns the declared 1-based line numbers, in declaration order
     */
    private extractCalloutLines(codeBlock: HTMLElement): number[] {
        return (codeBlock.dataset.callouts ?? '')
            .split(',')
            .map(lineNumber => parseInt(lineNumber))
            .filter(lineNumber => !isNaN(lineNumber));
    }

    /**
     * Creates a marker element displaying the given callout index.
     *
     * @param index the 1-based ordinal of the callout within its code block
     * @returns the marker element to append to the marked line
     */
    private createMarker(index: number): HTMLSpanElement {
        const marker = document.createElement('span');
        marker.className = CodeCallouts.MARKER_CLASS;
        marker.textContent = index.toString();
        return marker;
    }
}
