/**
 * Escapes HTML special characters to prevent XSS.
 * @param text - The text to escape
 * @returns The escaped HTML-safe string
 */
export function escapeHtml(text: string): string {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Escapes special regex characters in a string.
 * @param text - The string to escape
 * @returns The escaped string safe for use in a RegExp
 */
export function escapeRegExp(text: string): string {
    return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
