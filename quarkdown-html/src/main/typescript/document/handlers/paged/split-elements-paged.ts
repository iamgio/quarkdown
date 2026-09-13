import {DocumentHandler} from "../../document-handler";

/**
 * Represents a pair of elements where one was split from the other due to page breaks.
 */
export interface SplitElement<T extends HTMLElement = HTMLElement> {
    /** The original element that was split */
    from: T;
    /** The new element created from the split */
    split: T;
}

/**
 * Base document handler for elements that have been split across page breaks in paged media.
 *
 * When an element overflows a page, paged.js splits it: the portion carried over to the
 * new page is marked with a `data-split-from` attribute, whose value is the `data-ref`
 * attribute of the element it was split from.
 *
 * After rendering, this handler pairs each split portion with its original element,
 * and delegates element-specific adjustments to subclasses.
 *
 * @template T - The type of element this handler adjusts
 */
export abstract class SplitElementsPaged<T extends HTMLElement = HTMLElement> extends DocumentHandler {
    /**
     * CSS selector matching the kind of element this handler adjusts (e.g. `code`, `table`).
     */
    protected abstract readonly selector: string;

    /**
     * Applies element-specific adjustments to the given split element pairs.
     *
     * @param splitElements Array of split element pairs, each containing the original element and its split counterpart
     */
    protected abstract adjust(splitElements: SplitElement<T>[]): void;

    /**
     * Identifies and returns all elements matching [selector] that were split due to page breaks.
     *
     * @returns An array of split element pairs, each containing the original element and its split counterpart
     */
    private getSplitElements(): SplitElement<T>[] {
        const splitElements: SplitElement<T>[] = [];

        document.querySelectorAll<T>(`${this.selector}[data-split-from]`).forEach(split => {
            const fromRef = split.getAttribute('data-split-from');
            if (!fromRef) return;

            const from = document.querySelector<T>(`${this.selector}[data-ref="${fromRef}"]`);
            if (!from) return;

            splitElements.push({from, split});
        });

        return splitElements;
    }

    /**
     * Executes the split element adjustments after document rendering is complete,
     * once page breaks have been determined.
     */
    async onPostRendering() {
        this.adjust(this.getSplitElements());
    }
}
