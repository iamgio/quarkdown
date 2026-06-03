package com.quarkdown.core.util

import java.util.Locale

/**
 * @param prefix prefix to remove
 * @param ignoreCase whether to ignore case when searching for the prefix
 * @return a pair of this string without [prefix] and a boolean value indicating whether the prefix was removed.
 *         If the prefix is not present, the string is returned as is and the boolean value is `false`
 */
fun String.removeOptionalPrefix(
    prefix: String,
    ignoreCase: Boolean = false,
): Pair<String, Boolean> =
    if (startsWith(prefix, ignoreCase)) {
        substring(prefix.length) to true
    } else {
        this to false
    }

/**
 * @return a sliced copy of this string from start to the last occurrence of [string] if it exists,
 *         this string otherwise
 */
fun String.takeUntilLastOccurrence(string: String): String {
    // Trim trailing #s preceded by a space
    val trailingIndex = lastIndexOf(string)
    return if (trailingIndex >= 0) {
        substring(0, trailingIndex)
    } else {
        this
    }
}

/**
 * @return a substring of [this] string from [startIndex] to [endIndex] if the indices are within bounds.
 * If [startIndex] is less than 0, the actual start index is 0.
 * If [endIndex] is greater than the length of the string or less than the start index,
 * the actual end index is the length of the string.
 */
fun CharSequence.substringWithinBounds(
    startIndex: Int,
    endIndex: Int,
): String {
    val start = startIndex.coerceAtLeast(0)
    return substring(start, endIndex.coerceAtMost(length).coerceAtLeast(start))
}

/**
 * @return [this] string without the first and last characters, if possible
 */
fun String.trimDelimiters(): String = if (length >= 2) substring(1, length - 1) else this

/**
 * Indents each line of [this] string by [indent].
 * @param indent indentation string
 * @return [this] string, indented
 */
fun CharSequence.indent(indent: String) =
    buildString {
        this@indent
            .lineSequence()
            .filterNot { it.isEmpty() }
            .forEach { append(indent).append(it).append("\n") }
    }

/**
 * @param count number of lines to take
 * @param addOmittedLinesSuffix whether to add a suffix indicating how many lines were omitted
 * @return the first [count] lines of [this] string, plus an optional `... (N more lines)` suffix
 */
fun CharSequence.takeLines(
    count: Int,
    addOmittedLinesSuffix: Boolean,
): String {
    if (!addOmittedLinesSuffix) {
        return this.lines().take(count).joinToString(separator = "\n")
    }

    val lines = this.lines()
    return if (lines.size <= count) {
        this.toString()
    } else {
        buildString {
            lines.take(count).forEach { appendLine(it) }
            appendLine("... (${lines.size - count} more lines)")
        }
    }
}

/**
 * An optimized way to replace all occurrences of [oldValue] with [newValue] in a [StringBuilder].
 * @return this builder
 */
fun StringBuilder.replace(
    oldValue: String,
    newValue: String,
) = apply {
    var startIndex = indexOf(oldValue)
    while (startIndex >= 0) {
        replace(startIndex, startIndex + oldValue.length, newValue)
        startIndex = indexOf(oldValue, startIndex + newValue.length)
    }
}

/**
 * @return [this] string with all non-alphanumeric characters,
 *         except for `-`, `_`, `@`, replaced with [replacement].
 *         Alphanumeric characters include Unicode letters and numbers.
 *         `.` is sanitized only at the beginning and the end of the string.
 * @param replacement character to replace invalid characters with
 */
fun String.sanitizeFileName(replacement: String) = this.replace("^\\.|\\.$|[^\\p{L}\\p{N}\\p{M}\\-_.@]+".toRegex(), replacement)

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
    this
        .lowercase(Locale.ROOT)
        .replace("\\s+".toRegex(), "-")
        .replace("[^\\p{L}\\p{N}-]".toRegex(), "")

/**
 * Sanitizes [this] string for use as a stable, anchor-like identifier:
 * - Strips characters that are problematic in CSS selectors and URL fragments
 *   (whitespace, quotes, angle brackets, ampersands).
 * - Ensures the result is not empty: an empty input becomes `_`.
 * - Ensures the result does not start with a digit by prepending `_` when needed (see issue #86).
 *
 * Shared between rendering-time HTML id generation and deduplication keying, so that two
 * identifiers that sanitize to the same string are recognised as collisions before they reach the output.
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

/**
 * @return [this] string with line separators replaced with `\n`,
 *         or the string itself if `\n` is already the line separator
 */
fun CharSequence.normalizeLineSeparators(): CharSequence =
    when (val separator = System.lineSeparator()) {
        "\n" -> this
        else -> this.toString().replace(separator, "\n")
    }

/**
 * Discards blank entries and trims each remaining entry.
 * @return a list of non-blank, trimmed strings
 */
fun List<String>.trimEntries(): List<String> =
    filter { it.isNotBlank() }
        .map { it.trim() }
