/**
 * Generates a hash code from a given string.
 * @param str The input string.
 * @returns A hash code as a string.
 */
export function hashCode(str: string): string {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = ((hash << 5) - hash) + str.charCodeAt(i);
        hash |= 0;
    }
    return hash.toString();
}