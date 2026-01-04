import {DocumentHandler} from "../../document-handler";

const SEARCH_SHORTCUT_KEY = "/";

/**
 * Document handler that focuses the search field when '/' is pressed.
 * This is a common UX pattern for documentation sites.
 */
export class SearchFieldFocus extends DocumentHandler {
    async onPostRendering() {
        document.addEventListener("keydown", (event) => {
            if (event.key !== SEARCH_SHORTCUT_KEY) return;

            // Don't intercept if already typing in an input or textarea.
            const activeElement = document.activeElement;
            if (activeElement instanceof HTMLInputElement || activeElement instanceof HTMLTextAreaElement) {
                return;
            }

            event.preventDefault();
            document.getElementById("search-input")?.focus();
        });
    }
}
