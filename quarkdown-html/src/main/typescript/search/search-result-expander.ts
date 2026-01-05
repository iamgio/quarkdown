import {DocumentSearchResult} from "./search";
import {escapeHtml, extractPreviewAroundMatch, highlightTerms, trimTitleFromContent,} from "./search-highlight";

/**
 * A display item for rendering in the search results dropdown.
 */
export interface DisplayItem {
    url: string;
    title: string;
    description: string;
    /** For heading results: the parent page title */
    parentTitle?: string;
}

/**
 * Expands a search result into display items, creating separate items for heading matches.
 * @param result - The search result to expand
 * @returns Array of display items
 */
export function expandResult(result: DocumentSearchResult): DisplayItem[] {
    const {entry, matchedFields} = result;
    const items: DisplayItem[] = [];

    // Add the main page result
    items.push({
        url: entry.url,
        title: entry.title ?? entry.url,
        description: getHighlightedDescription(result),
    });

    // Add separate items for heading matches
    const headingTerms = getTermsForField(matchedFields, "headings");
    if (headingTerms.length > 0) {
        const matchingHeadings = findMatchingHeadings(entry.headings, headingTerms);
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
function getTermsForField(matchedFields: Record<string, string[]>, field: string): string[] {
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
function findMatchingHeadings(
    headings: DocumentSearchResult["entry"]["headings"],
    terms: string[]
): DocumentSearchResult["entry"]["headings"] {
    return headings.filter((heading) =>
        terms.some((term) => heading.text.toLowerCase().includes(term.toLowerCase()))
    );
}

/**
 * Checks if any match came from the title or headings.
 * @param matchedFields - Map of terms to fields they matched in
 * @returns True if any match is from the document title or headings
 */
function isTitleOrHeadingMatch(matchedFields: Record<string, string[]>): boolean {
    const flattened = Object.values(matchedFields).flat();
    return flattened.includes("title") || flattened.includes("headings");
}

/**
 * Generates a highlighted description for a search result.
 * @param result - The search result
 * @returns HTML string with highlighted matches
 */
function getHighlightedDescription(result: DocumentSearchResult): string {
    const {entry, matchedTerms, matchedFields} = result;
    const text = entry.description ?? trimTitleFromContent(entry.content, entry.title);
    if (!text) return "";

    // If match is from the title or headings, don't highlight anything
    if (isTitleOrHeadingMatch(matchedFields)) {
        const preview = extractPreviewAroundMatch(text, []);
        return escapeHtml(preview);
    }

    const preview = extractPreviewAroundMatch(text, matchedTerms);
    return highlightTerms(preview, matchedTerms);
}
