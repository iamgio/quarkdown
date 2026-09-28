package com.quarkdown.core.lexer.scan

private const val CUSTOM_ID_BEGIN = '{'

private const val CUSTOM_ID_MARKER = '#'

private const val CUSTOM_ID_END = '}'

/**
 * The outcome of looking for a `{#custom-id}` suffix.
 * @param contentEnd index just past the content that precedes the suffix, the spaces and tabs before the
 *                   brace excluded
 * @param value the identifier, or `null` when there is no suffix
 */
class TrailingCustomId(
    val contentEnd: Int,
    val value: String?,
)

/**
 * Parsing of the `{#custom-id}` cross-reference suffix.
 */
object CustomIds {
    /**
     * Looks for a suffix at the end of one line's slice, without allocating the slice.
     * @param line line whose slice to inspect
     * @param from index of the slice's first character, which the suffix may not start before
     * @param end index just past the slice's last character
     * @return the parsed suffix, or one with `value` set to `null` and `contentEnd` set to [end]
     */
    fun trailing(
        line: Line,
        from: Int,
        end: Int,
    ): TrailingCustomId {
        val source = line.sourceOf()
        if (end - 1 < from || source[end - 1] != CUSTOM_ID_END) return TrailingCustomId(end, null)
        var open = end - 2
        while (open > from && source[open] != CUSTOM_ID_BEGIN && source[open] != CUSTOM_ID_END) open--
        val isSuffix =
            open >= from &&
                source[open] == CUSTOM_ID_BEGIN &&
                open + 1 < end &&
                source[open + 1] == CUSTOM_ID_MARKER &&
                open + 2 < end - 1
        if (!isSuffix) return TrailingCustomId(end, null)
        var contentEnd = open
        while (contentEnd > from && (source[contentEnd - 1] == ' ' || source[contentEnd - 1] == '\t')) contentEnd--
        return TrailingCustomId(contentEnd, source.substring(open + 2, end - 1))
    }
}

/**
 * Consumes a `{#identifier}` suffix.
 * @return the identifier, or `null` when none starts here
 */
fun Scanner.takeCustomId(): String? =
    attempt {
        if (!take(CUSTOM_ID_BEGIN) || !take(CUSTOM_ID_MARKER)) return@attempt null
        val mark = mark()
        takeWhile { it != CUSTOM_ID_END }
        val identifier = sliceFrom(mark)
        if (identifier.isEmpty() || !take(CUSTOM_ID_END)) return@attempt null
        identifier
    }
