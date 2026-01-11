import {DisplayItem} from "./search-result-expander";
import {escapeHtml} from "./search-highlight";

/**
 * Renders a single search result item as an HTML string.
 * @param item - The display item to render
 * @param index - The index of the result for keyboard navigation
 * @returns HTML string for the result item
 */
export function renderResultItem(item: DisplayItem, index: number): string {
    const className = item.parentTitle ? "search-result search-result-heading" : "search-result";
    const titleHtml = item.parentTitle
        ? `${escapeHtml(item.parentTitle)}<span class="search-result-chevron"></span>${escapeHtml(item.title)}`
        : escapeHtml(item.title);

    return `<a href="${escapeHtml(item.url)}" class="${className}" role="option" data-index="${index}">
        <div class="search-result-title">${titleHtml}</div>
        ${item.description ? `<div class="search-result-description">${item.description}</div>` : ""}
    </a>`;
}

/**
 * Renders an array of display items into HTML.
 * @param items - Array of display items to render
 * @returns Combined HTML string for all items
 */
export function renderResultItems(items: DisplayItem[]): string {
    return items.map((item, index) => renderResultItem(item, index)).join("");
}
