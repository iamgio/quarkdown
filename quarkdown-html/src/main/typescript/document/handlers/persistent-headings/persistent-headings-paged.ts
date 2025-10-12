import {PersistentHeadingsDocumentHandler} from "./persistent-headings-document-handler";

/**
 * Handles persistent headings for paged media.
 */
export class PersistentHeadingsPaged extends PersistentHeadingsDocumentHandler {
    /**
     * Post-rendering hook that applies persistent headings to each page.
     */
    async onPostRendering() {
        const pages = document.querySelectorAll('.pagedjs_page');

        pages.forEach(page => {
            this.apply({
                sourceContainer: page,
                targetContainers: [page],
            });
        });
    }
}