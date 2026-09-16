/**
 * Handler of single nodes rendered by paged.js while a page is being laid out.
 *
 * Unlike document handlers, which run before or after the whole rendering,
 * node handlers take part in the layout itself, as changes they apply are
 * accounted for when distributing content across pages.
 *
 * @template T - The type of node this handler accepts
 */
export abstract class PagedNodeHandler<T extends Node = Node> {
    /**
     * @returns Whether this handler applies to the given rendered node,
     *          narrowing it to the type this handler works with
     */
    abstract accepts(clone: Node): clone is T;

    /**
     * Applies changes to each accepted node cloned into the page being laid out.
     * @param clone node rendered into the page
     * @param source corresponding node in the source document
     */
    abstract render(clone: T, source: Node): void;
}
