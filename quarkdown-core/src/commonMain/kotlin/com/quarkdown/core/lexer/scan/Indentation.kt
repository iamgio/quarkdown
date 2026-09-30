package com.quarkdown.core.lexer.scan

/**
 * Width of a tab stop, in columns, as defined by CommonMark.
 */
const val TAB_WIDTH = 4

/**
 * Highest indentation, in columns, a block-level construct may have before it becomes indented code.
 */
const val MAX_BLOCK_INDENT = 3

/**
 * Column arithmetic over raw source text.
 */
object Indentation {
    /**
     * Skips leading whitespace for as long as it spans fewer than [columns] columns, counting a tab up to the
     * next [TAB_WIDTH] stop. A tab is never split: it is taken whole or not at all, which is why the limit is
     * reached exactly when it is a multiple of [TAB_WIDTH].
     * @param source text to inspect
     * @param start index of the slice's first character
     * @param end index just past the slice's last character
     * @param columns how many columns of whitespace to skip at most
     * @return index just past the skipped whitespace
     */
    fun endOfColumns(
        source: CharSequence,
        start: Int,
        end: Int,
        columns: Int,
    ): Int {
        var skipped = 0
        var index = start
        while (index < end && skipped < columns) {
            skipped +=
                when (source[index]) {
                    ' ' -> 1
                    '\t' -> TAB_WIDTH - skipped % TAB_WIDTH
                    else -> return index
                }
            index++
        }
        return index
    }
}
