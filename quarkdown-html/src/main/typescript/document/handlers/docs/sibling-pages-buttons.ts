import {DocumentHandler} from "../../document-handler";
import {PageListAnalyzer} from "./util/page-list-analyzer";

const BUTTON_AREA_ID = "sibling-pages-button-area";
const PREVIOUS_LINK_ID = "previous-page-anchor";
const NEXT_LINK_ID = "next-page-anchor";
const PREVIOUS_ICON_CLASS = "bi bi-arrow-left";
const NEXT_ICON_CLASS = "bi bi-arrow-right";

/**
 * Document handler that creates previous/next page navigation links.
 * Uses PageListAnalyzer to find sibling pages and adds styled links
 * to the #sibling-pages-button-area element.
 */
export class SiblingPagesButtons extends DocumentHandler {
    async onPostRendering() {
        const buttonArea = document.getElementById(BUTTON_AREA_ID);
        if (!buttonArea) return;

        const analyzer = new PageListAnalyzer();

        const previousLink = analyzer.getPreviousPageLink();
        if (previousLink) {
            buttonArea.appendChild(this.createAnchor(previousLink, PREVIOUS_LINK_ID, PREVIOUS_ICON_CLASS, "start"));
        }

        const nextLink = analyzer.getNextPageLink();
        if (nextLink) {
            buttonArea.appendChild(this.createAnchor(nextLink, NEXT_LINK_ID, NEXT_ICON_CLASS, "end"));
        }
    }

    /**
     * Creates a cloned anchor element with an icon.
     * @param anchor - The anchor element to clone
     * @param anchorId - The ID to assign to the anchor
     * @param iconClass - The Bootstrap icon class
     * @param iconPosition - Where to place the icon ("start" or "end")
     */
    private createAnchor(
        anchor: HTMLAnchorElement,
        anchorId: string,
        iconClass: string,
        iconPosition: "start" | "end",
    ): HTMLAnchorElement {
        const clonedAnchor = anchor.cloneNode(true) as HTMLAnchorElement;
        clonedAnchor.id = anchorId;

        const icon = document.createElement("i");
        icon.className = iconClass;

        if (iconPosition === "start") {
            clonedAnchor.insertBefore(icon, clonedAnchor.firstChild);
        } else {
            clonedAnchor.appendChild(icon);
        }

        return clonedAnchor;
    }
}
