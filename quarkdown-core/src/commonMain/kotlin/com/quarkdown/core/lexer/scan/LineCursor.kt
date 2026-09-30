package com.quarkdown.core.lexer.scan

/**
 * An immutable view over the lines of a source, positioned at one of them.
 * Recognizers receive a cursor and never mutate it, so no state is shared between them.
 * @param lines every line of the source
 * @param index index of the line the cursor points at
 */
class LineCursor(
    private val lines: List<Line>,
    val index: Int,
) {
    /**
     * The line the cursor points at.
     */
    val current: Line get() = lines[index]

    /**
     * Amount of lines from the cursor to the end of the source, the current one included.
     */
    val remaining: Int get() = lines.size - index

    /**
     * @param offset lines past the current one
     * @return the line at [offset], or `null` when it falls outside the source
     */
    fun peek(offset: Int): Line? = lines.getOrNull(index + offset)

    /**
     * @param offset lines past the current one
     * @return a cursor at [offset], or `null` when it falls outside the source
     */
    fun at(offset: Int): LineCursor? = if (index + offset in lines.indices) LineCursor(lines, index + offset) else null

    /**
     * @param lineCount amount of lines, starting from the current one
     * @return the index just past the last character of those lines, line terminators included
     */
    fun spanTo(lineCount: Int): Int = lines[index + lineCount - 1].nextStart

    /**
     * @param index an index within the source
     * @return the amount of lines, counted from the cursor, whose span reaches [index], or `null` when
     *         [index] lies past the end of the source
     */
    fun lineCountThrough(index: Int): Int? {
        var count = 1
        while (true) {
            val line = peek(count - 1) ?: return null
            if (index <= line.nextStart) return count
            count++
        }
    }
}
