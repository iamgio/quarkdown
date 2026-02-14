import {DocumentHandler} from "../../document-handler";
import {initNavigationActiveTracking} from "../../../navigation/active-tracking";

const TOC_SELECTOR = 'aside nav[data-role="table-of-contents"]';

/**
 * Document handler that highlights the table of contents entry
 * corresponding to the currently visible section, using scroll-based tracking.
 */
export class TocActiveTracking extends DocumentHandler {
    async onPostRendering() {
        const toc = document.querySelector<HTMLElement>(TOC_SELECTOR);
        if (!toc) return;

        initNavigationActiveTracking(toc);
    }
}
