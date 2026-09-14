import {SplitElement, SplitElementsPaged} from "./split-elements-paged";

/**
 * Elements that constitute visible content on their own, even without any text.
 */
const VISUAL_CONTENT_SELECTOR = 'img, svg, video, canvas, iframe';

/**
 * Base document handler for captioned elements that have been split across page breaks.
 *
 * The caption of a captioned element, such as a table's `<caption>` or a figure's
 * `<figcaption>`, follows the element's content in the DOM, so page breaks carry it
 * over to the last portion regardless of the side it is displayed on.
 *
 * This handler moves the caption to the portion matching its displayed position:
 * a top caption to the first portion, a bottom caption to the last one.
 * Portions that display no content are not eligible, so that the caption is never
 * left alone on a page, split from the content it describes; if such a portion
 * is left empty, it is removed.
 *
 * @template T - The type of element this handler adjusts
 */
export abstract class SplitCaptionedElementsPaged<T extends HTMLElement = HTMLElement> extends SplitElementsPaged<T> {
    /**
     * @returns Whether the given portion displays any content beside the caption:
     *          non-blank text, or a visual element such as an image
     */
    private hasContent(portion: T, caption: HTMLElement): boolean {
        return Array.from(portion.children).some(child =>
            child !== caption && (
                (child.textContent?.trim() ?? '') !== ''
                || child.matches(VISUAL_CONTENT_SELECTOR)
                || child.querySelector(VISUAL_CONTENT_SELECTOR) !== null
            )
        );
    }

    /**
     * Moves the caption of each split element to the portion matching its displayed side,
     * and drops the portions left displaying nothing.
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

            // Only portions that display content can host the caption,
            // so that it is never shown on its own, apart from the content.
            // If no portion displays content, the caption is left untouched.
            const candidates = parts.filter(part => this.hasContent(part, caption));
            if (candidates.length === 0) return;

            // The renderer marks every caption with its position via a `caption-top` or `caption-bottom` class.
            const target = caption.classList.contains('caption-bottom') ? candidates[candidates.length - 1] : candidates[0];
            if (caption.parentElement !== target) {
                target.prepend(caption);
            }

            // Portions left with neither content nor the caption display nothing: drop them.
            parts
                .filter(part => part !== target && !this.hasContent(part, caption))
                .forEach(part => part.remove());
        });
    }

    protected adjust(splitElements: SplitElement<T>[]) {
        this.repositionCaptions(splitElements);
    }
}
