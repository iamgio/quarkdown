import {DocumentHandler} from "../document-handler";
import {PagedLikeQuarkdownDocument, QuarkdownPage} from "../paged-like-quarkdown-document";

const MIN_HEADING_LEVEL = 1;
const MAX_HEADING_LEVEL = 6;

/**
 * Handler that manages persistent headings across pages.
 * Maintains a history of the most recent heading at each depth level and applies
 * them to elements with the `.last-heading` class.
 */
export class PersistentHeadings extends DocumentHandler<PagedLikeQuarkdownDocument<any>> {
    /**
     * Array storing the most recent heading HTML content at each depth level.
     * Index 0 corresponds to h1, index 1 to h2, etc.
     */
    protected readonly lastHeadingPerDepth: string[] = [];

    /**
     * Scans a page for headings (h1-h6) and updates the internal heading history.
     * Only the last heading of the highest level found is stored, and lower level headings are cleared.
     *
     * @example
     * If the container has:
     * ```html
     * <h2>Title</h2>
     * <h3>Subtitle</h3>
     * <h2>Another Title</h2>
     * ```
     *
     * Then after calling this method, `lastHeadingPerDepth` will be:
     * ```typescript
     * ["", "Another Title", "", "", "", ""] // h1 is empty, h2 is "Another Title", h3 has been cleared
     * ```
     *
     * @param page - The page to scan for headings
     */
    private overwriteLastHeadings(page: QuarkdownPage) {
        // Find the highest level non-decorative heading in the container (h1 to h6).
        for (let depth = MIN_HEADING_LEVEL; depth <= MAX_HEADING_LEVEL; depth++) {
            const headings = page.querySelectorAll(`h${depth}:not([data-decorative])`);
            if (headings.length > 0) {
                this.lastHeadingPerDepth[depth - 1] = headings[headings.length - 1].innerHTML;
                this.lastHeadingPerDepth.length = depth; // Remove lower level headings.
            }
        }
    }

    /**
     * Applies the stored heading content to elements with the `.last-heading` class
     * within the specified containers. The heading content is determined by the
     * `data-depth` attribute on each `.last-heading` element.
     * @param page - The page containing `.last-heading` elements to update
     */
    private applyLastHeadings(page: QuarkdownPage) {
        const lastHeadingElements = page.querySelectorAll('.last-heading');
        lastHeadingElements.forEach(lastHeading => {
            const depth = parseInt(lastHeading.dataset.depth || '0');
            lastHeading.innerHTML = this.lastHeadingPerDepth[depth - 1] || '';
        });
    }

    async onPostRendering() {
        const pages = this.quarkdownDocument.getPages();
        pages.forEach(page => {
            this.overwriteLastHeadings(page);
            this.applyLastHeadings(page);
        });
    }
}