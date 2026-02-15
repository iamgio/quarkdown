import {romanize} from "romans";

/**
 * Formats a number according to the specified numbering format.
 * This mirrors the behavior from the main compiler.
 * Supported formats:
 * - 1 (arabic)
 * - a (lower-alpha)
 * - A (upper-alpha)
 * - i (lower-roman)
 * - I (upper-roman)
 * For unknown formats the format string itself is returned.
 * @param number The number to format.
 * @param format The format to use for formatting the page number.
 * @returns The formatted page number as a string.
 */
export function formatNumber(number: number, format: string): string {
    switch (format) {
        case "1":
            return number.toString();
        case "a":
            return String.fromCharCode('a'.charCodeAt(0) + number - 1);
        case "A":
            return String.fromCharCode('A'.charCodeAt(0) + number - 1);
        case "i":
            return romanize(number).toLowerCase();
        case "I":
            return romanize(number);
        default:
            return format;
    }
}
