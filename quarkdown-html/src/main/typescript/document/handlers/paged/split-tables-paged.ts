import {SplitElement} from "./split-elements-paged";
import {SplitCaptionedElementsPaged} from "./split-captioned-elements-paged";

/**
 * Document handler that adjusts tables that have been split across page breaks.
 *
 * When a table is split due to page breaks in paged media, the portion carried over
 * to the new page loses the header row, which remains only in the original table.
 * This handler copies the header row of the original table into each split portion,
 * so that every page repeats the header row.
 *
 * On top of that, the caption, if any, is moved to the portion matching its position,
 * as provided by [SplitCaptionedElementsPaged].
 */
export class SplitTablesPaged extends SplitCaptionedElementsPaged<HTMLTableElement> {
    protected readonly selector = 'table';

    /**
     * Copies the header row of the original table into its split counterparts
     * that lack one, so that every page repeats the header row.
     *
     * @param splitTables Array of split table pairs to adjust
     */
    private repeatHeaders(splitTables: SplitElement<HTMLTableElement>[]) {
        splitTables.forEach(({from, split}) => {
            const header = from.querySelector(':scope > thead');
            if (!header || split.querySelector(':scope > thead')) return;

            split.prepend(header.cloneNode(true));
        });
    }

    protected adjust(splitTables: SplitElement<HTMLTableElement>[]) {
        this.repeatHeaders(splitTables);
        super.adjust(splitTables);
    }
}
