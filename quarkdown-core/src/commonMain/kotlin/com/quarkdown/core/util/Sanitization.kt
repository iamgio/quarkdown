package com.quarkdown.core.util

// A negated class must not mix `\p{...}` members with literal ones: the Kotlin/Wasm regex engine mishandles it.
// Keeping the two groups in separate classes joined by `&&` works on every target.

/**
 * Matches a leading or trailing `.`, or a run of characters that are not
 * letters, numbers, combining marks or one of `-`, `_`, `.`, `@`.
 */
private val INVALID_FILE_NAME_CHARS = "^\\.|\\.$|[[^\\p{IsL}\\p{IsN}\\p{IsM}]&&[^\\-_.@]]+".toRegex()

/**
 * Matches a character that is not a letter, a number or `-`.
 */
private val INVALID_URI_IDENTIFIER_CHARS = "[[^\\p{IsL}\\p{IsN}]&&[^-]]".toRegex()

private val WHITESPACE = "\\s+".toRegex()

/**
 * @return [this] string with all non-alphanumeric characters,
 *         except for `-`, `_`, `@`, replaced with [replacement].
 *         Alphanumeric characters include Unicode letters, numbers and combining marks.
 *         Each run of consecutive invalid characters is replaced with a single [replacement].
 *         `.` is sanitized only at the beginning and the end of the string.
 * @param replacement character to replace invalid characters with
 */
fun String.sanitizeFileName(replacement: String): String = replace(INVALID_FILE_NAME_CHARS, replacement)

/**
 * Converts [this] string to a URI-like identifier:
 * - Lowercased
 * - Whitespace runs are replaced with dashes.
 * - Characters other than Unicode letters, Unicode digits and `-` are stripped.
 *
 * Example: `"Hello, World!"` -> `"hello-world"`.
 * @return URI-like identifier string
 */
fun String.toUriIdentifier(): String =
    lowercase()
        .replace(WHITESPACE, "-")
        .replace(INVALID_URI_IDENTIFIER_CHARS, "")

/**
 * Sanitizes [this] string for use as a stable, anchor-like identifier:
 * - Strips characters that are problematic in CSS selectors and URL fragments
 *   (whitespace, quotes, angle brackets, ampersands).
 * - Ensures the result is not empty: an empty input becomes `_`.
 * - Ensures the result does not start with a digit by prepending `_` when needed (see issue #86).
 * @return a safe identifier string, possibly different from the original
 */
fun String.sanitizeAsIdentifier(): String {
    val stripped = this.replace("[\\s\"'<>&]".toRegex(), "")
    return when {
        stripped.isEmpty() -> "_"
        stripped.first().isDigit() -> "_$stripped"
        else -> stripped
    }
}
