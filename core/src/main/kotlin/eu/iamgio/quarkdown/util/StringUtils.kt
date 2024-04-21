package eu.iamgio.quarkdown.util

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
 * @return [this] string with line separators replaced with `\n`,
 *         or the string itself if `\n` is already the line separator
 */
fun CharSequence.normalizeLineSeparators(): CharSequence =
    when (val separator = System.lineSeparator()) {
        "\n" -> this
        else -> this.toString().replace(separator, "\n")
    }
