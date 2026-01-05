/**
 * Utilities for highlighting search terms in text previews.
 */

import {escapeHtml, escapeRegExp} from "../util/escape";

export {escapeHtml};

/**
 * Removes the title from the beginning of the content, if present.
 * This handles the case where plain text extraction includes the H1 title.
 * @param content - The full content text
 * @param title - The entry title to trim
 * @returns Content with title trimmed from the start
 */
export function trimTitleFromContent(content: string, title: string | null): string {
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
 * @param maxLength - Maximum length of the preview
 * @returns A truncated preview with ellipsis if needed
 */
export function extractPreviewAroundMatch(
    content: string,
    matchedTerms: string[],
    maxLength: number = 300
): string {
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
export function highlightTerms(text: string, matchedTerms: string[]): string {
    if (matchedTerms.length === 0) return escapeHtml(text);

    // Sort terms by length (longest first) to avoid partial replacements
    const sortedTerms = [...matchedTerms].sort((a, b) => b.length - a.length);
    const pattern = new RegExp(`(${sortedTerms.map((t) => escapeRegExp(t)).join("|")})`, "gi");

    // Split by matches and process each part
    const parts = text.split(pattern);
    return parts
        .map((part) => {
            const isMatch = sortedTerms.some((term) => part.toLowerCase() === term.toLowerCase());
            const escaped = escapeHtml(part);
            return isMatch ? `<strong>${escaped}</strong>` : escaped;
        })
        .join("");
}
