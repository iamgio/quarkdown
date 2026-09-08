import {DocumentHandler} from "../../document-handler";

/**
 * Class of the scroll container that tables are wrapped in.
 * Styled in the docs stylesheet with horizontal overflow scrolling.
 */
const WRAPPER_CLASS = "table-scroll-area";

/**
 * Document handler that wraps each content table in a horizontally scrollable container.
 *
 * Tables cannot act as scroll containers themselves, so a table wider than the content
 * area would otherwise overlap the sidebar and make the whole page horizontally scrollable.
 *
 * Tables inside code blocks (e.g. line-numbered code) are excluded.
 */
export class ScrollableTables extends DocumentHandler {
    async onPostRendering() {
        document
            .querySelectorAll<HTMLTableElement>(".quarkdown-docs > .content-wrapper > main table")
            .forEach((table) => this.wrap(table));
    }

    /**
     * Wraps a table in a scrollable container, unless it is part of a code block
     * or already wrapped.
     * @param table - The table to wrap
     */
    private wrap(table: HTMLTableElement) {
        if (table.closest("pre") || table.parentElement?.classList.contains(WRAPPER_CLASS)) return;

        const wrapper = document.createElement("div");
        wrapper.className = WRAPPER_CLASS;
        table.replaceWith(wrapper);
        wrapper.appendChild(table);
    }
}
