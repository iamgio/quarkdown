import {DocumentHandler} from "../../document-handler";
import {PagedLikeQuarkdownDocument, QuarkdownPage} from "../../paged-like-quarkdown-document";

/**
 * Abstract base class for document handlers that manage page margin content.
 * Collects page margin initializers during pre-rendering and positions them appropriately
 * on each page in the final document based on the document type.
 *
 * In case of `outside`/`inside` positions, the `data-on-left-page` and `data-on-right-page` may differ.
 */
export abstract class PageMarginsDocumentHandler extends DocumentHandler<PagedLikeQuarkdownDocument<any>> {
    /** Array of page margin initializer elements collected from the document */
    protected pageMarginInitializers: HTMLElement[] = [];

    /**
     * Collects all page margin content initializers and removes them from the document.
     * This prevents them from being displayed before proper positioning.
     */
    async onPreRendering() {
        this.pageMarginInitializers = Array.from(document.querySelectorAll('.page-margin-content'));
        this.pageMarginInitializers.forEach(initializer => initializer.remove());
    }

    /**
     * Called after the main rendering process is complete,
     * this function is responsible for injecting page margin content
     * into the document at appropriate locations on each page.
     */
    async onPostRendering() {
        this.quarkdownDocument.getPages().forEach(page => {
            this.pageMarginInitializers.forEach(initializer => {
                const marginPositionName = this.getMarginPositionName(initializer, page);
                if (!marginPositionName) return;

                this.apply(initializer, page, marginPositionName);
            });
        });
    }

    /**
     * Gets the margin position name for the given page margin initializer, depending on whether the page is left or right.
     * @param initializer The page margin initializer element
     * @param page The page the margin will be applied to
     * @return The margin position name (e.g., "top-left", "bottom-center"), if defined
     */
    private getMarginPositionName(initializer: HTMLElement, page: QuarkdownPage): string | null {
        const pageType = this.quarkdownDocument.getPageType(page);
        return initializer.getAttribute(`data-on-${pageType}-page`);
    }

    /**
     * Copies the class list from the initializer to the target margin element,
     * adding the specific margin position class.
     * @param target The target margin element to which classes will be added
     * @param initializer The page margin initializer element
     * @param marginPositionName The margin position name (e.g., "top-left", "bottom-center")
     */
    protected pushMarginClassList(target: HTMLElement, initializer: HTMLElement, marginPositionName: string) {
        target.classList.add(
            `page-margin-${marginPositionName}`, // In case of mirror positions (outside/inside), sets the actual position.
            ...initializer.classList,
        );
    }

    /**
     * Applies the page margin initializer to the given page.
     * This method must be implemented by subclasses to define
     * how the margin content is injected into the page.
     * @param initializer The page margin initializer element
     * @param page The page to apply the margin to
     * @param marginPositionName The margin position name (e.g., "top-left", "bottom-center")
     */
    abstract apply(initializer: HTMLElement, page: QuarkdownPage, marginPositionName: string): void;
}