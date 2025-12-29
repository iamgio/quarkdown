package com.quarkdown.core.util

import java.net.MalformedURLException
import java.net.URL

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
    // Trim trailing #s preceeded by a space
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
 *         `.` is sanitized only at the beginning and the end of the string.
 * @param replacement character to replace invalid characters with
 */
fun String.sanitizeFileName(replacement: String) = this.replace("^\\.|\\.$|[^a-zA-Z0-9\\-_.@]+".toRegex(), replacement)

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
 * @return a URL from [this] string if it's a valid URL, or `null` otherwise
 */
fun String.toURLOrNull(): URL? =
    try {
        URL(this)
    } catch (_: MalformedURLException) {
        null
    }

/**
 * Whether [this] string is a valid URL.
 */
val String.isURL: Boolean
    get() = toURLOrNull() != null
