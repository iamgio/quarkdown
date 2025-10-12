import {DocumentHandler} from "../../document-handler";

/**
 * Document handler that sets the column count for paged documents.
 *
 * This handler reads the `--qd-column-count` CSS variable
 * and applies it to each page's content area after rendering. This ensures that the column
 * layout is correctly applied in paged media.
 *
 * For some unknown reason, this has to be applied after the page is rendered to avoid visual glitches.
 * For non-paged documents, the column count is applied directly via CSS instead (see _viewport.scss).
 */
export class ColumnCountPaged extends DocumentHandler {
    async onPostRendering() {
        const columnCount = getComputedStyle(document.body).getPropertyValue('--qd-column-count')?.trim()
        if (!columnCount || columnCount === '') return; // No value set.

        document.querySelectorAll<HTMLElement>('.pagedjs_page_content > div').forEach(content => {
            content.style.columnCount = columnCount;
        });
    }
}