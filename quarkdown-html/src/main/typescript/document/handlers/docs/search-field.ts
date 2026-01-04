import {DocumentHandler} from "../../document-handler";
import {createSearch, DocumentSearch, DocumentSearchResult} from "../../../search/search";

const SEARCH_INPUT_ID = "search-input";
const SEARCH_RESULTS_ID = "search-results";
const SEARCH_INDEX_META_NAME = "quarkdown:search-index";

const DEBOUNCE_MS = 150;
const MAX_RESULTS = 10;

/**
 * Document handler that initializes search functionality for documentation sites.
 * Fetches the search index from the meta tag and displays results in a dropdown.
 */
export class SearchField extends DocumentHandler {
    private search: DocumentSearch | null = null;
    private input: HTMLInputElement | null = null;
    private resultsContainer: HTMLElement | null = null;
    private debounceTimeout: ReturnType<typeof setTimeout> | null = null;
    private selectedIndex = -1;

    async onPostRendering() {
        this.input = document.getElementById(SEARCH_INPUT_ID) as HTMLInputElement | null;
        if (!this.input) return;

        const indexPath = this.getSearchIndexPath();
        if (!indexPath) return;

        await this.initializeSearch(indexPath);
        this.createResultsContainer();
        this.bindEvents();
    }

    /**
     * Retrieves the search index path from the meta tag.
     * @returns The path to the search index JSON, or null if not found
     */
    private getSearchIndexPath(): string | null {
        const meta = document.querySelector(`meta[name="${SEARCH_INDEX_META_NAME}"]`);
        return meta?.getAttribute("content") ?? null;
    }

    /**
     * Fetches and initializes the search index from the given path.
     * @param indexPath - URL path to the search index JSON file
     */
    private async initializeSearch(indexPath: string): Promise<void> {
        const response = await fetch(indexPath);
        if (!response.ok) return;

        const index = await response.json();
        this.search = createSearch(index, {maxResults: MAX_RESULTS});
    }

    /**
     * Creates the results dropdown container and appends it to the search wrapper.
     */
    private createResultsContainer(): void {
        const wrapper = this.input!.closest(".search-wrapper");
        if (!wrapper) return;

        this.resultsContainer = document.createElement("div");
        this.resultsContainer.id = SEARCH_RESULTS_ID;
        this.resultsContainer.setAttribute("role", "listbox");
        this.resultsContainer.hidden = true;
        wrapper.appendChild(this.resultsContainer);
    }

    /**
     * Binds event listeners for search input, keyboard navigation, and blur handling.
     */
    private bindEvents(): void {
        this.input!.addEventListener("input", () => this.onInputChange());
        this.input!.addEventListener("keydown", (e) => this.onKeyDown(e));
        this.input!.addEventListener("blur", () => this.hideResultsDelayed());
        this.resultsContainer!.addEventListener("mousedown", (e) => e.preventDefault());
    }

    /**
     * Handles input changes with debouncing to avoid excessive searches.
     */
    private onInputChange(): void {
        if (this.debounceTimeout) clearTimeout(this.debounceTimeout);
        this.debounceTimeout = setTimeout(() => this.performSearch(), DEBOUNCE_MS);
    }

    /**
     * Executes the search query and renders the results.
     */
    private performSearch(): void {
        const query = this.input!.value.trim();
        if (!query || !this.search) {
            this.hideResults();
            return;
        }

        const results = this.search.search(query);
        this.renderResults(results);
    }

    /**
     * Renders search results into the dropdown container.
     * @param results - Array of search results to display
     */
    private renderResults(results: DocumentSearchResult[]): void {
        if (results.length === 0) {
            this.hideResults();
            return;
        }

        this.selectedIndex = -1;
        this.resultsContainer!.innerHTML = results
            .map((result, index) => this.renderResultItem(result, index))
            .join("");
        this.resultsContainer!.hidden = false;
    }

    /**
     * Renders a single search result item as an HTML string.
     * @param result - The search result to render
     * @param index - The index of the result for keyboard navigation
     * @returns HTML string for the result item
     */
    private renderResultItem(result: DocumentSearchResult, index: number): string {
        const {entry, matchedTerms} = result;
        const title = entry.title ?? entry.url;
        const description = this.getHighlightedDescription(entry, matchedTerms);

        return `<a href="${entry.url}" class="search-result" role="option" data-index="${index}">
            <span class="search-result-title">${this.escapeHtml(title)}</span>
            ${description ? `<span class="search-result-description">${description}</span>` : ""}
        </a>`;
    }

    /**
     * Generates a highlighted description with matched terms in bold.
     * @param entry - The search entry containing description and content
     * @param matchedTerms - Array of terms that matched the search query
     * @returns HTML string with highlighted matches
     */
    private getHighlightedDescription(entry: DocumentSearchResult["entry"], matchedTerms: string[]): string {
        const text = entry.description ?? entry.content;
        if (!text) return "";

        const preview = this.extractPreviewAroundMatch(text, matchedTerms);
        return this.highlightTerms(preview, matchedTerms);
    }

    /**
     * Extracts a preview of the content centered around the first match.
     * @param content - The full content text
     * @param matchedTerms - Array of matched search terms
     * @returns A truncated preview with ellipsis if needed
     */
    private extractPreviewAroundMatch(content: string, matchedTerms: string[]): string {
        const maxLength = 300;
        if (content.length <= maxLength) return content;

        // Find the first matching term in the content
        const lowerContent = content.toLowerCase();
        let firstMatchIndex = -1;

        for (const term of matchedTerms) {
            const index = lowerContent.indexOf(term.toLowerCase());
            if (index !== -1 && (firstMatchIndex === -1 || index < firstMatchIndex)) {
                firstMatchIndex = index;
            }
        }

        // If no match found, return from the beginning
        if (firstMatchIndex === -1) {
            return content.slice(0, maxLength).trimEnd() + "…";
        }

        // Center the preview around the match
        const halfLength = Math.floor(maxLength / 2);
        let start = Math.max(0, firstMatchIndex - halfLength);
        let end = Math.min(content.length, start + maxLength);

        // Adjust start if we're near the end
        if (end === content.length) {
            start = Math.max(0, end - maxLength);
        }

        let preview = content.slice(start, end);

        // Add ellipsis
        if (start > 0) preview = "…" + preview.trimStart();
        if (end < content.length) preview = preview.trimEnd() + "…";

        return preview;
    }

    /**
     * Wraps matched terms in the text with strong tags for highlighting.
     * @param text - The text to process
     * @param matchedTerms - Array of terms to highlight
     * @returns HTML string with matches wrapped in strong tags
     */
    private highlightTerms(text: string, matchedTerms: string[]): string {
        if (matchedTerms.length === 0) return this.escapeHtml(text);

        // Sort terms by length (longest first) to avoid partial replacements
        const sortedTerms = [...matchedTerms].sort((a, b) => b.length - a.length);
        const pattern = new RegExp(`(${sortedTerms.map(t => this.escapeRegExp(t)).join("|")})`, "gi");

        // Split by matches and process each part
        const parts = text.split(pattern);
        return parts
            .map((part) => {
                const isMatch = sortedTerms.some((term) => part.toLowerCase() === term.toLowerCase());
                const escaped = this.escapeHtml(part);
                return isMatch ? `<strong>${escaped}</strong>` : escaped;
            })
            .join("");
    }

    /**
     * Escapes special regex characters in a string.
     * @param text - The string to escape
     * @returns The escaped string safe for use in a RegExp
     */
    private escapeRegExp(text: string): string {
        return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    }

    /**
     * Escapes HTML special characters to prevent XSS.
     * @param text - The text to escape
     * @returns The escaped HTML-safe string
     */
    private escapeHtml(text: string): string {
        const div = document.createElement("div");
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Handles keyboard navigation within the search results.
     * @param event - The keyboard event
     */
    private onKeyDown(event: KeyboardEvent): void {
        if (this.resultsContainer!.hidden) return;

        const items = this.resultsContainer!.querySelectorAll<HTMLElement>(".search-result");
        if (items.length === 0) return;

        switch (event.key) {
            case "ArrowDown":
                event.preventDefault();
                this.selectItem(items, Math.min(this.selectedIndex + 1, items.length - 1));
                break;
            case "ArrowUp":
                event.preventDefault();
                this.selectItem(items, Math.max(this.selectedIndex - 1, 0));
                break;
            case "Enter":
                if (this.selectedIndex >= 0) {
                    event.preventDefault();
                    items[this.selectedIndex].click();
                }
                break;
            case "Escape":
                this.hideResults();
                break;
        }
    }

    /**
     * Updates the selected item in the results list.
     * @param items - The list of result elements
     * @param index - The index to select
     */
    private selectItem(items: NodeListOf<HTMLElement>, index: number): void {
        items.forEach((item, i) => item.classList.toggle("selected", i === index));
        this.selectedIndex = index;
    }

    /**
     * Hides the results dropdown and resets selection.
     */
    private hideResults(): void {
        this.resultsContainer!.hidden = true;
        this.selectedIndex = -1;
    }

    /**
     * Hides results after a short delay to allow click events to fire.
     */
    private hideResultsDelayed(): void {
        setTimeout(() => this.hideResults(), 150);
    }
}
