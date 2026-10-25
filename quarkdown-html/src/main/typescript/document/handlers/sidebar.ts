import {DocumentHandler} from "../document-handler";
import {initNavigationActiveTracking} from "../../navigation/active-tracking";
import {markWhileSelectingOutside} from "../../navigation/selection-drag";

/**
 * Document handler responsible for relocating the sidebar from the template,
 * initializing active state tracking, and keeping the sidebar out of the way while the user selects content.
 */
export class Sidebar extends DocumentHandler {
    async onPostRendering() {
        const template = document.querySelector<HTMLTemplateElement>('#sidebar-template');
        if (!template) return;

        const sidebar = template.content.firstElementChild?.cloneNode(true) as HTMLElement;
        if (!sidebar) return;

        sidebar.style.position = "fixed";
        document.body.appendChild(sidebar);
        template.remove();

        initNavigationActiveTracking(sidebar);
        markWhileSelectingOutside(sidebar);
    }
}
