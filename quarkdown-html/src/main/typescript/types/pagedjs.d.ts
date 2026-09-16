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
    }

    export function registerHandlers(...handlers: (typeof Handler)[]): void;

    const Paged: {
        Handler: typeof Handler;
        registerHandlers: typeof registerHandlers;
    };

    export default Paged;
}