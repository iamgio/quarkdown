package com.quarkdown.core.lexer.scan.inline

import com.quarkdown.core.lexer.scan.CharCategories

internal const val STRIKETHROUGH_DELIMITER = '~'

private const val UNDERSCORE_DELIMITER = '_'

private const val STRIKETHROUGH_LENGTH = 2

/**
 * A run of identical emphasis delimiter characters, classified per CommonMark 0.31.2 section 6.2.
 *
 * The run is mutable because the pairing algorithm consumes delimiters from its inner end: an opener loses
 * its rightmost characters and a closer its leftmost. [originalLength] keeps the length as found, which is
 * what the rule of three is defined on.
 *
 * @param char the delimiter character
 * @param originalLength length of the run as it was found
 * @param canOpen whether the run may open emphasis
 * @param canClose whether the run may close emphasis
 * @param start index of the run's first remaining character
 * @param length amount of remaining characters
 */
class DelimiterRun(
    val char: Char,
    val originalLength: Int,
    val canOpen: Boolean,
    val canClose: Boolean,
    var start: Int,
    var length: Int,
) {
    /**
     * Index just past the run's last remaining character.
     */
    val end: Int get() = start + length

    /**
     * Whether the run is still on the delimiter stack. A run leaves the stack once it is fully consumed or
     * once the algorithm decides it can never match.
     */
    var isActive: Boolean = true
}

/**
 * Classification of delimiter runs.
 * A position outside the source counts as whitespace, which is how CommonMark treats the start and the end
 * of a line.
 */
object DelimiterRuns {
    /**
     * @param source text to inspect
     * @param index index the run must start at
     * @param char the run's character
     * @return the length of the run of [char] starting at [index], possibly zero
     */
    fun runLengthOf(
        source: CharSequence,
        index: Int,
        char: Char,
    ): Int {
        var cursor = index
        while (cursor < source.length && source[cursor] == char) cursor++
        return cursor - index
    }

    /**
     * @param source text to inspect
     * @param index index to test
     * @return whether the index is the start of the source or follows a line terminator
     */
    fun isLineStart(
        source: CharSequence,
        index: Int,
    ): Boolean = index == 0 || source.getOrNull(index - 1) == '\n'

    /**
     * Classifies the delimiter run starting at an index.
     *
     * A run that can neither open nor close is still returned, with both flags false, so that the caller
     * consumes it whole as text: re-entering the scan inside a run would classify its remainder against the
     * wrong neighbours, and `a__b`'s second underscore would look like an opener.
     *
     * @param source text to inspect
     * @param index index the run must start at
     * @param char the run's character
     * @return the classified run, or `null` when [char] is not the character at [index]
     */
    fun classify(
        source: CharSequence,
        index: Int,
        char: Char,
    ): DelimiterRun? {
        if (source.getOrNull(index) != char) return null
        val length = runLengthOf(source, index, char)
        // Quarkdown's strikethrough accepts runs of exactly two tildes; any other run is literal text.
        val pairable = char != STRIKETHROUGH_DELIMITER || length == STRIKETHROUGH_LENGTH
        val before = source.getOrNull(index - 1)
        val after = source.getOrNull(index + length)
        val leftFlanking =
            !after.isWhitespaceOrEdge() &&
                (!after.isPunctuation() || before.isWhitespaceOrEdge() || before.isPunctuation())
        val rightFlanking =
            !before.isWhitespaceOrEdge() &&
                (!before.isPunctuation() || after.isWhitespaceOrEdge() || after.isPunctuation())
        val canOpen =
            pairable &&
                when (char) {
                    UNDERSCORE_DELIMITER -> leftFlanking && (!rightFlanking || before.isPunctuation())
                    else -> leftFlanking
                }
        val canClose =
            pairable &&
                when (char) {
                    UNDERSCORE_DELIMITER -> rightFlanking && (!leftFlanking || after.isPunctuation())
                    else -> rightFlanking
                }
        return DelimiterRun(char, length, canOpen, canClose, index, length)
    }
}

/**
 * This check is Unicode-aware, because CommonMark 0.31.2 defines a flanking delimiter run's boundaries in
 * terms of Unicode whitespace.
 * @return whether the character is Unicode whitespace or outside the source, the edges counting as whitespace
 */
private fun Char?.isWhitespaceOrEdge(): Boolean = this == null || isWhitespace()

private fun Char?.isPunctuation(): Boolean = this != null && CharCategories.isPunctuationOrSymbol(this)
