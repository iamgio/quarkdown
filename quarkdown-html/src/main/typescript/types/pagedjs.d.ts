/** Type definitions for pagedjs */
declare module "pagedjs" {
    /**
     * Base class for paged.js handlers, whose hook methods
     * are called during the pagination lifecycle.
     */
    export class Handler {
        /** Called after the whole document has been rendered. */
        afterRendered?(): void;

        /** Called for each node cloned into the page being laid out. */
        renderNode?(clone: Node, source: Node): void;

        /**
         * Called whenever paged.js checks a page for overflow.
         * @param overflow range from the first overflowing content to the end of the page, or `undefined` if nothing overflows
         * @param rendered the page content being laid out
         * @param bounds bounds of the page content box
         * @returns a range replacing the overflow range, or `undefined` to keep it
         */
        onOverflow?(overflow: Range | undefined, rendered: Element, bounds: DOMRect): Range | undefined;
    }

    export function registerHandlers(...handlers: (typeof Handler)[]): void;

    const Paged: {
        Handler: typeof Handler;
        registerHandlers: typeof registerHandlers;
    };

    export default Paged;
}