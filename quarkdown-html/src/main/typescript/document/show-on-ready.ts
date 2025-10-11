import {DocumentHandler} from "./document-handler";

/**
 * A document handler that hides the body until the document is fully rendered.
 * This prevents flickering and unfinished content from being visible to the user.
 */
export class ShowOnReady extends DocumentHandler {
    async onPreRendering() {
        document.body.style.opacity = "0";
    }

    async onPostRendering() {
        document.body.style.opacity = "1";
    }
}