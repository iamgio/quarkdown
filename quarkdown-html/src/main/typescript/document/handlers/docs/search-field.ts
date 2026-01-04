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

    private getSearchIndexPath(): string | null {
        const meta = document.querySelector(`meta[name="${SEARCH_INDEX_META_NAME}"]`);
        return meta?.getAttribute("content") ?? null;
    }

    private async initializeSearch(indexPath: string): Promise<void> {
        const response = await fetch(indexPath);
        if (!response.ok) return;

        const index = await response.json();
        this.search = createSearch(index, {maxResults: MAX_RESULTS});
    }

    private createResultsContainer(): void {
        const wrapper = this.input!.closest(".search-wrapper");
        if (!wrapper) return;

        this.resultsContainer = document.createElement("div");
        this.resultsContainer.id = SEARCH_RESULTS_ID;
        this.resultsContainer.setAttribute("role", "listbox");
        this.resultsContainer.hidden = true;
        wrapper.appendChild(this.resultsContainer);
    }

    private bindEvents(): void {
        this.input!.addEventListener("input", () => this.onInputChange());
        this.input!.addEventListener("keydown", (e) => this.onKeyDown(e));
        this.input!.addEventListener("blur", () => this.hideResultsDelayed());
        this.resultsContainer!.addEventListener("mousedown", (e) => e.preventDefault());
    }

    private onInputChange(): void {
        if (this.debounceTimeout) clearTimeout(this.debounceTimeout);
        this.debounceTimeout = setTimeout(() => this.performSearch(), DEBOUNCE_MS);
    }

    private performSearch(): void {
        const query = this.input!.value.trim();
        if (!query || !this.search) {
            this.hideResults();
            return;
        }

        const results = this.search.search(query);
        this.renderResults(results);
    }

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

    private renderResultItem(result: DocumentSearchResult, index: number): string {
        const {entry} = result;
        const title = entry.title ?? entry.url;
        const description = entry.description ?? this.getContentPreview(entry.content);

        return `<a href="${entry.url}" class="search-result" role="option" data-index="${index}">
            <span class="search-result-title">${this.escapeHtml(title)}</span>
            ${description ? `<span class="search-result-description">${this.escapeHtml(description)}</span>` : ""}
        </a>`;
    }

    private getContentPreview(content: string): string {
        const maxLength = 300;
        if (content.length <= maxLength) return content;
        return content.slice(0, maxLength).trimEnd() + "…";
    }

    private escapeHtml(text: string): string {
        const div = document.createElement("div");
        div.textContent = text;
        return div.innerHTML;
    }

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

    private selectItem(items: NodeListOf<HTMLElement>, index: number): void {
        items.forEach((item, i) => item.classList.toggle("selected", i === index));
        this.selectedIndex = index;
    }

    private hideResults(): void {
        this.resultsContainer!.hidden = true;
        this.selectedIndex = -1;
    }

    private hideResultsDelayed(): void {
        setTimeout(() => this.hideResults(), 150);
    }
}
