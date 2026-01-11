import {DocumentHandler} from "../../document-handler";

// Base selectors
const HEADER = 'body > header';
const CONTENT = 'body > .content-wrapper';
const SIDEBAR_LEFT = `${CONTENT} > aside:first-child`;
const SIDEBAR_RIGHT = `${CONTENT} > aside:last-child`;
const FOOTER = `${CONTENT} > main > footer`;

/**
 * Maps margin position names to their target container selectors.
 */
const MARGIN_TARGETS: Record<string, string> = {
    // Header
    'top-left-corner': `${HEADER} > aside:first-child`,
    'top-left': `${HEADER} > aside:first-child`,
    'top-center': `${HEADER} > main`,
    'top-right-corner': `${HEADER} > aside:last-child`,
    'top-right': `${HEADER} > aside:last-child`,

    // Left sidebar
    'left-top': `${SIDEBAR_LEFT} > .position-top`,
    'left-middle': `${SIDEBAR_LEFT} > .position-middle`,
    'left-bottom': `${SIDEBAR_LEFT} > .position-bottom`,
    'bottom-left-corner': `${SIDEBAR_LEFT} > .position-bottom`,

    // Right sidebar
    'right-top': `${SIDEBAR_RIGHT} > .position-top`,
    'right-middle': `${SIDEBAR_RIGHT} > .position-middle`,
    'right-bottom': `${SIDEBAR_RIGHT} > .position-bottom`,
    'bottom-right-corner': `${SIDEBAR_RIGHT} > .position-bottom`,

    // Footer
    'bottom-left': `${FOOTER} > .position-left`,
    'bottom-center': `${FOOTER} > .position-center`,
    'bottom-right': `${FOOTER} > .position-right`,
};

/**
 * Page margins handler for documentation sites.
 * Unlike paged/slides implementations, docs have fixed containers without pagination.
 * This handler moves margin content into predefined containers in the document structure.
 */
export class PageMarginsDocs extends DocumentHandler {

    async onPostRendering(): Promise<void> {
        document.querySelectorAll<HTMLElement>('.page-margin-content').forEach((initializer) => {
            const position = this.getMarginPosition(initializer);
            if (!position) return;
            initializer.remove();

            const selector = MARGIN_TARGETS[position];
            if (!selector) return;

            const container = document.querySelector(selector);
            if (!container) return;

            container.appendChild(this.createWrapper(initializer, position));
        });
    }

    /**
     * Creates a wrapper element with the appropriate classes and content.
     */
    private createWrapper(initializer: HTMLElement, position: string): HTMLElement {
        const wrapper = document.createElement('div');
        wrapper.classList.add(`page-margin-${position}`, ...initializer.classList);
        wrapper.innerHTML = initializer.innerHTML;
        return wrapper;
    }

    /**
     * Gets the margin position name from the initializer element.
     * Docs don't have left/right pages, so either data attribute works.
     */
    private getMarginPosition(initializer: HTMLElement): string | null {
        return initializer.dataset.onLeftPage ?? initializer.dataset.onRightPage ?? null;
    }
}
