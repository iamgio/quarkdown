/** Type definitions for pagedjs */
declare module "pagedjs" {
    export class Handler {
        afterRendered(): void;
    }

    export function registerHandlers(handler: typeof Handler): void;

    const Paged: {
        Handler: typeof Handler;
        registerHandlers: typeof registerHandlers;
    };

    export default Paged;
}