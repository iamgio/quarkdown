package com.quarkdown.core.lexer.scan

/**
 * A single line of a scanned source, with its indentation precomputed.
 * Lines are produced once per source by [Lines.of] and never re-sliced.
 * @param source the whole source the line belongs to
 * @param start index of the line's first character within [source]
 * @param end index just past the line's last character, line terminator excluded
 * @param nextStart index of the next line's first character, or [end] at the end of input
 */
class Line(
    private val source: CharSequence,
    val start: Int,
    val end: Int,
    val nextStart: Int,
) {
    /**
     * Columns of leading whitespace, tabs expanded to [TAB_WIDTH] stops.
     */
    val indent: Int

    /**
     * Index of the first non-whitespace character, or [end] when the line is blank.
     */
    val contentStart: Int

    /**
     * Whether the line holds no non-whitespace character.
     */
    val isBlank: Boolean get() = contentStart == end

    /**
     * Whether the line is terminated by a newline, as opposed to ending at the end of input.
     */
    val isTerminated: Boolean get() = nextStart > end

    /**
     * Whether the line holds no character at all, as opposed to [isBlank], which also holds for a line of
     * whitespace.
     */
    val isEmpty: Boolean get() = start == end

    /**
     * Amount of literal spaces the line starts with, tabs excluded. A list's resumption is defined in
     * spaces, where [indent] is defined in columns.
     */
    val leadingSpaces: Int

    init {
        var columns = 0
        var spaces = 0
        var index = start
        var countingSpaces = true
        while (index < end) {
            when (source[index]) {
                ' ' -> {
                    columns++
                    if (countingSpaces) spaces++
                }

                '\t' -> {
                    columns += TAB_WIDTH - columns % TAB_WIDTH
                    countingSpaces = false
                }

                else -> break
            }
            index++
        }
        indent = columns
        contentStart = index
        leadingSpaces = spaces
    }

    private var textCache: String? = null

    private var contentCache: String? = null

    /**
     * Raw text of the line, terminator excluded. Allocates on first access.
     */
    val text: String get() = textCache ?: source.substring(start, end).also { textCache = it }

    /**
     * Text of the line after its leading whitespace, terminator excluded. Allocates on first access.
     */
    val content: String get() = contentCache ?: source.substring(contentStart, end).also { contentCache = it }

    /**
     * @param index absolute index within the source
     * @return the character at [index], or `null` when it falls outside this line
     */
    fun charAt(index: Int): Char? = source.getOrNull(index)?.takeIf { index in start until end }

    /**
     * @param char character to count
     * @return the length of the run of [char] that starts the line's content, possibly zero
     */
    fun countContentRun(char: Char): Int {
        var index = contentStart
        while (index < end && source[index] == char) index++
        return index - contentStart
    }

    /**
     * @param start index of the slice's first character
     * @param end index just past the slice's last character
     * @return the source slice, which may extend beyond this line
     */
    fun sourceSlice(
        start: Int,
        end: Int,
    ): String = source.substring(start, end)

    /**
     * @return the whole source this line belongs to, for helpers that parse with absolute indices
     */
    fun sourceOf(): CharSequence = source
}

/**
 * Factory of [Line]s.
 */
object Lines {
    /**
     * Splits a source into lines. The source is expected to have normalized line separators, so only
     * `\n` terminates a line. A source ending with a newline produces no trailing empty line, and an
     * empty source produces no lines.
     * @param source text to split
     * @return the lines of [source], in order
     */
    fun of(source: CharSequence): List<Line> {
        val lines = ArrayList<Line>(source.length / 32 + 1)
        var start = 0
        while (start < source.length) {
            val terminator = source.indexOf('\n', start)
            val end = if (terminator < 0) source.length else terminator
            val nextStart = if (terminator < 0) source.length else terminator + 1
            lines += Line(source, start, end, nextStart)
            start = nextStart
        }
        return lines
    }
}

/**
 * A construct that may span lines ends where a paragraph does, which is at a line holding nothing.
 * @return whether a line break is followed, after spaces and tabs, by another line break
 */
internal fun CharSequence.holdsBlankLine(): Boolean {
    var index = indexOf('\n')
    while (index >= 0) {
        var next = index + 1
        while (next < length && (this[next] == ' ' || this[next] == '\t')) next++
        if (next < length && this[next] == '\n') return true
        index = indexOf('\n', next)
    }
    return false
}
