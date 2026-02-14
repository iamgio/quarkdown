/**
 * Converts an integer to a Roman numeral string.
 */
function toRomanNumeral(num: number): string {
    if (isNaN(num))
        return "NaN";
    const digits = String(+num).split("");
    const key = ["", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM",
            "", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC",
            "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"];
    let roman = "";
    let i = 3;
    while (i--)
        roman = (key[+digits.pop()! + (i * 10)] || "") + roman;
    return Array(+digits.join("") + 1).join("M") + roman;
}

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
            return toRomanNumeral(pageNumber).toLowerCase();
        case "I":
            return toRomanNumeral(pageNumber);
        default:
            return format;
    }
}
