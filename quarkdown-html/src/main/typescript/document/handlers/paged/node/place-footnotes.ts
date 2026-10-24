import {PagedNodeHandler} from "./paged-node-handler";
import {DetachedFootnotes, FOOTNOTE_ANCHOR_ATTRIBUTE} from "../../footnotes/detached-footnotes";
import {getOrCreateFootnoteRule, removeFootnoteRule} from "../../../../footnotes/footnote-dom";
import {isInOverflowColumn} from "../overflow-column";

const ANCHOR_SELECTOR = `.footnote-reference[${FOOTNOTE_ANCHOR_ATTRIBUTE}]`;

/** Fraction of the page area a footnote area may take at most, so that a huge footnote cannot starve the page of content. */
const MAX_AREA_RATIO = 0.5;

/**
 * Node handler that places each footnote definition into the footnote area of the page
 * its anchor reference is rendered on, while that page is being laid out.
 *
 * The page content shrinks right away, so paged.js accounts for the exact space the footnote takes
 * when distributing the remaining content. If paged.js later carries the reference over to the next page,
 * the definition follows it and the previous page regains its space.
 */
export class PlaceFootnotes extends PagedNodeHandler<Element> {
    constructor(private readonly footnotes: DetachedFootnotes) {
        super();
    }

    accepts(clone: Node): clone is Element {
        return clone instanceof Element && (clone.matches(ANCHOR_SELECTOR) || clone.querySelector(ANCHOR_SELECTOR) !== null);
    }

    render(clone: Element) {
        const anchors = clone.matches(ANCHOR_SELECTOR) ? [clone] : Array.from(clone.querySelectorAll(ANCHOR_SELECTOR));
        anchors.forEach(anchor => this.place(anchor));
    }

    private place(anchor: Element) {
        const definition = this.footnotes.definitionOf(anchor);
        const area = anchor.closest<HTMLElement>('.pagedjs_area');
        if (!definition || !area || isCarriedOver(anchor, area)) return;

        const footnoteArea = footnoteAreaOf(area);
        if (!footnoteArea || definition.parentElement === footnoteArea.inner) return;

        const previousArea = definition.closest<HTMLElement>('.pagedjs_area');
        footnoteArea.inner.appendChild(definition);
        updateFootnoteArea(area);
        if (previousArea && previousArea !== area) updateFootnoteArea(previousArea);
    }
}

/** The paged.js footnote area of a page: the content box and the container definitions are placed into. */
interface FootnoteArea {
    content: HTMLElement;
    inner: HTMLElement;
}

function footnoteAreaOf(area: HTMLElement): FootnoteArea | undefined {
    const content = area.querySelector<HTMLElement>('.pagedjs_footnote_area > .pagedjs_footnote_content');
    const inner = content?.querySelector<HTMLElement>('.pagedjs_footnote_inner_content');
    return content && inner ? {content, inner} : undefined;
}

/**
 * Resizes the footnote area of a page to fit the definitions it holds, which shrinks the page content accordingly,
 * and shows or hides the area depending on whether it holds any definition.
 * @param area page area (`.pagedjs_area`) to update
 */
function updateFootnoteArea(area: HTMLElement) {
    const footnoteArea = footnoteAreaOf(area);
    if (!footnoteArea) return;
    const {content, inner} = footnoteArea;

    if (!inner.querySelector('.footnote-definition')) {
        removeFootnoteRule(inner);
        content.classList.add('pagedjs_footnote_empty');
        area.style.setProperty('--pagedjs-footnotes-height', '0px');
        return;
    }

    getOrCreateFootnoteRule(inner);
    // The empty state hides the content, which would measure as zero.
    content.classList.remove('pagedjs_footnote_empty');
    const height = content.scrollHeight + verticalMargins(content);
    area.style.setProperty('--pagedjs-footnotes-height', `${Math.min(height, area.clientHeight * MAX_AREA_RATIO)}px`);
}

/**
 * @returns the sum of the vertical margins of the element, which the footnote area must make room for
 *          as paged.js's own footnote module does
 */
function verticalMargins(element: HTMLElement): number {
    const styles = getComputedStyle(element);
    return (parseFloat(styles.marginTop) || 0) + (parseFloat(styles.marginBottom) || 0);
}

/**
 * @returns whether the anchor was rendered in the hidden overflow column of the page,
 *          meaning paged.js will carry it over to the next page
 */
function isCarriedOver(anchor: Element, area: HTMLElement): boolean {
    const content = area.querySelector('.pagedjs_page_content')?.getBoundingClientRect();
    return !!content && content.width > 0 && isInOverflowColumn(anchor.getBoundingClientRect(), content.right);
}
