import {romanize} from "romans";

/**
 * Formats a page number according to the specified format.
 * Supported formats: "1" (arabic), "a" (lower-alpha), "A" (upper-alpha), "i" (lower-roman), "I" (upper-roman).
 * For unknown formats the format string itself is returned.
 * @param pageNumber The page number to format.
 * @param format The format to use for formatting the page number.
 * @returns The formatted page number as a string.
 */
export function formatPageNumber(pageNumber: number, format: string): string {
    switch (format) {
        case "1":
            return pageNumber.toString();
        case "a":
            return String.fromCharCode(96 + pageNumber);
        case "A":
            return String.fromCharCode(64 + pageNumber);
        case "i":
            return romanize(pageNumber).toLowerCase();
        case "I":
            return romanize(pageNumber);
        default:
            return format;
    }
}
