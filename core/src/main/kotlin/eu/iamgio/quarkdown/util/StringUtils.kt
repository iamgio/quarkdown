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
