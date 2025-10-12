import {DocumentHandler} from "../document-handler";
import {createSidebar} from "../../sidebar/sidebar";

/**
 * Document handler responsible for creating and managing the sidebar component.
 * Executes after document rendering is complete.
 * @see createSidebar
 */
export class Sidebar extends DocumentHandler {
    async onPostRendering() {
        createSidebar();
    }
}