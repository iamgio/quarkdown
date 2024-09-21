package eu.iamgio.quarkdown.util

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
): Pair<String, Boolean> {
    return if (startsWith(prefix, ignoreCase)) {
        substring(prefix.length) to true
    } else {
        this to false
    }
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
        this@indent.lineSequence()
            .filterNot { it.isEmpty() }
            .forEach { append(indent).append(it).append("\n") }
    }

/**
 * An optimized way to replace all occurrences of [oldValue] with [newValue] in a [StringBuilder].
 * @return this builder
 */
fun StringBuilder.replace(
    oldValue: String,
    newValue: String,
) = apply {
    var index: Int
    while (indexOf(oldValue).also { index = it } >= 0) {
        replace(index, index + oldValue.length, newValue)
    }
}

/**
 * @return [this] string with all non-alphanumeric characters,
 *         except for `-`, `_`, `@`, and `.`, replaced with [replacement]
 * @param replacement character to replace invalid characters with
 */
fun String.sanitizeFileName(replacement: String) = this.replace("[^a-zA-Z0-9\\-_.@]+".toRegex(), replacement)

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
    } catch (e: MalformedURLException) {
        null
    }
