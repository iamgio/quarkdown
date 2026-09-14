import {SplitElement, SplitElementsPaged} from "./split-elements-paged";

/**
 * Base document handler for captioned elements that have been split across page breaks.
 *
 * The caption of a captioned element, such as a table's `<caption>` or a figure's
 * `<figcaption>`, follows the element's content in the DOM, so page breaks carry it
 * over to the last portion regardless of the side it is displayed on.
 *
 * This handler moves the caption to the portion matching its displayed position:
 * a top caption to the first portion, a bottom caption to the last one.
 *
 * @template T - The type of element this handler adjusts
 */
export abstract class SplitCaptionedElementsPaged<T extends HTMLElement = HTMLElement> extends SplitElementsPaged<T> {
    /**
     * Moves the caption of each split element to the portion matching its displayed side.
     *
     * @param splitElements Array of split element pairs to adjust
     */
    private repositionCaptions(splitElements: SplitElement<T>[]) {
        // All portions of each element, in document order: the original first, then its splits.
        const portions = new Map<T, T[]>();
        splitElements.forEach(({from, split}) => {
            if (!portions.has(from)) portions.set(from, [from]);
            portions.get(from)!.push(split);
        });

        portions.forEach(parts => {
            const caption = parts
                .map(part => part.querySelector<HTMLElement>(':scope > :is(caption, figcaption)'))
                .find(Boolean);
            if (!caption) return;

            // The renderer marks every caption with its position via a `caption-top` or `caption-bottom` class.
            const target = caption.classList.contains('caption-bottom') ? parts[parts.length - 1] : parts[0];
            if (caption.parentElement !== target) {
                target.prepend(caption);
            }
        });
    }

    protected adjust(splitElements: SplitElement<T>[]) {
        this.repositionCaptions(splitElements);
    }
}
