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
