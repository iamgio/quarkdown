import {DocumentHandler} from "../../document-handler";
import {createSearch, DocumentSearch, DocumentSearchResult} from "../../../search/search";

/**
 * A display item for rendering in the search results dropdown.
 */
interface DisplayItem {
    url: string;
    title: string;
    description: string;
    /** For heading results: the parent page title */
    parentTitle?: string;
}

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

        const displayItems = results.flatMap((result) => this.expandResult(result));

        this.selectedIndex = -1;
        this.resultsContainer!.innerHTML = displayItems
            .map((item, index) => this.renderResultItem(item, index))
            .join("");
        this.resultsContainer!.hidden = false;
    }

    /**
     * Expands a search result into display items, creating separate items for heading matches.
     * @param result - The search result to expand
     * @returns Array of display items
     */
    private expandResult(result: DocumentSearchResult): DisplayItem[] {
        const {entry, matchedFields} = result;
        const items: DisplayItem[] = [];

        // Add the main page result
        items.push({
            url: entry.url,
            title: entry.title ?? entry.url,
            description: this.getHighlightedDescription(result),
        });

        // Add separate items for heading matches
        const headingTerms = this.getTermsForField(matchedFields, "headings");
        if (headingTerms.length > 0) {
            const matchingHeadings = this.findMatchingHeadings(entry.headings, headingTerms);
            for (const heading of matchingHeadings) {
                items.push({
                    url: `${entry.url}#${heading.anchor}`,
                    title: heading.text,
                    description: "",
                    parentTitle: entry.title ?? entry.url,
                });
            }
        }

        return items;
    }

    /**
     * Gets the terms that matched in a specific field.
     * @param matchedFields - Map of terms to fields they matched in
     * @param field - The field to get terms for
     * @returns Array of terms that matched in the specified field
     */
    private getTermsForField(matchedFields: Record<string, string[]>, field: string): string[] {
        return Object.entries(matchedFields)
            .filter(([, fields]) => fields.includes(field))
            .map(([term]) => term);
    }

    /**
     * Finds headings that match any of the search terms.
     * @param headings - Array of headings to search
     * @param terms - Array of terms to match against
     * @returns Headings that contain any of the terms
     */
    private findMatchingHeadings(
        headings: DocumentSearchResult["entry"]["headings"],
        terms: string[]
    ): DocumentSearchResult["entry"]["headings"] {
        return headings.filter((heading) =>
            terms.some((term) => heading.text.toLowerCase().includes(term.toLowerCase()))
        );
    }

    /**
     * Renders a single search result item as an HTML string.
     * @param item - The display item to render
     * @param index - The index of the result for keyboard navigation
     * @returns HTML string for the result item
     */
    private renderResultItem(item: DisplayItem, index: number): string {
        const className = item.parentTitle ? "search-result search-result-heading" : "search-result";
        const titleHtml = item.parentTitle
            ? `${this.escapeHtml(item.parentTitle)}<span class="search-result-chevron"></span>${this.escapeHtml(item.title)}`
            : this.escapeHtml(item.title);

        return `<a href="${this.escapeHtml(item.url)}" class="${className}" role="option" data-index="${index}">
            <span class="search-result-title">${titleHtml}</span>
            ${item.description ? `<span class="search-result-description">${item.description}</span>` : ""}
        </a>`;
    }

    /**
     * Generates a highlighted description with matched terms in bold.
     * @param result - The search result containing entry and match info
     * @returns HTML string with highlighted matches
     */
    private getHighlightedDescription(result: DocumentSearchResult): string {
        const {entry, matchedTerms, matchedFields} = result;
        const text = entry.description ?? this.trimTitleFromContent(entry.content, entry.title);
        if (!text) return "";

        // If match is from the title or headings, don't highlight anything
        if (this.isTitleMatch(matchedFields)) {
            const preview = this.extractPreviewAroundMatch(text, []);
            return this.escapeHtml(preview);
        }

        const preview = this.extractPreviewAroundMatch(text, matchedTerms);
        return this.highlightTerms(preview, matchedTerms);
    }

    /**
     * Checks if any match came from the title or headings.
     * @param matchedFields - Map of terms to fields they matched in
     * @returns True if any match is from the document title or headings
     */
    private isTitleMatch(matchedFields: Record<string, string[]>): boolean {
        const flattened = Object.values(matchedFields).flat();
        return flattened.includes("title") || flattened.includes("headings");
    }

    /**
     * Removes the title from the beginning of the content, if present.
     * This is caused by the plain text extraction including the H1 title.
     * @param content - The full content text
     * @param title - The entry title to trim
     * @returns Content with title trimmed from the start
     */
    private trimTitleFromContent(content: string, title: string | null): string {
        if (!title || !content) return content;

        const trimmedContent = content.trimStart();
        if (trimmedContent.toLowerCase().startsWith(title.toLowerCase())) {
            return trimmedContent.slice(title.length).trimStart();
        }
        return content;
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
