import {DocumentHandler} from "../../document-handler";
import {createSearch, DocumentSearch, DocumentSearchResult} from "../../../search/search";
import {expandResult} from "../../../search/search-result-expander";
import {renderResultItems} from "../../../search/search-result-renderer";
import {getMetaContent, getRootPath} from "../../../util/meta";

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
        return getMetaContent(SEARCH_INDEX_META_NAME);
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

        const displayItems = results
            .flatMap((result) => expandResult(result))
            .map((item) => ({...item, url: this.resolveUrl(item.url)}));

        this.selectedIndex = -1;
        this.resultsContainer!.innerHTML = renderResultItems(displayItems);
        this.resultsContainer!.hidden = false;
    }

    /**
     * Resolves a URL to be relative to the current page's parent directory.
     * URLs starting with '/' are converted to relative paths.
     * @param url - The URL to resolve
     * @returns The resolved relative URL
     */
    private resolveUrl(url: string): string {
        if (!url.startsWith("/")) return url;
        return getRootPath() + url;
    }

    /**
     * Handles keyboard navigation within the search results.
     * @param event - The keyboard event
     */
    private onKeyDown(event: KeyboardEvent): void {
        if (this.resultsContainer!.hidden) return;

        const items = this.resultsContainer!.querySelectorAll<HTMLElement>(".search-result");
        if (items.length === 0) return;

        const lastIndex = items.length - 1;

        switch (event.key) {
            case "ArrowDown":
                event.preventDefault();
                this.selectItem(items, this.selectedIndex < lastIndex ? this.selectedIndex + 1 : 0);
                break;
            case "ArrowUp":
                event.preventDefault();
                this.selectItem(items, this.selectedIndex > 0 ? this.selectedIndex - 1 : lastIndex);
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
        items[index].scrollIntoView({block: "nearest"});
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
