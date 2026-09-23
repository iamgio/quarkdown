package com.quarkdown.core.parser.walker

/**
 * Utilities for grammar parsing.
 */
object GrammarUtils {
    /**
     * Matches a character if it is not escaped.
     * @param string the string to match
     * @param position the position of the character to match
     * @param char the character to match
     * @param onMatch optional action to perform if the character is matched
     * @return 1 if the character is matched and not preceded by an escape character, 0 otherwise
     */
    fun unescapedMatch(
        string: CharSequence,
        position: Int,
        char: Char,
        onMatch: () -> Unit = {},
    ): Int =
        when {
            string[position] != char -> 0
            string.getOrNull(position - 1) != '\\' -> {
                onMatch()
                1
            }

            else -> 0
        }

    /**
     * Matches a balanced sequence delimited by [begin] and [end], ignoring escaped delimiters.
     *
     * Scans forward starting at [position] and returns the number of characters up to,
     * but not including, the balancing end delimiter when the delimiters are balanced.
     * Returns 0 if no balanced end is found.
     *
     * Rules:
     * - A delimiter preceded by a backslash (\) is ignored.
     * - Nested delimiters are supported and adjust the depth accordingly.
     *
     * @param string the source to scan
     * @param position the starting index to scan from
     * @param begin the opening delimiter
     * @param end the closing delimiter
     * @return the length from [position] to the matching end delimiter, or 0 if none
     */
    fun balancedDelimitersMatch(
        string: CharSequence,
        position: Int,
        begin: Char,
        end: Char,
    ): Int {
        var depth = 0

        for (x in position until string.length) {
            when {
                // Unescaped begin delimiter.
                unescapedMatch(string, x, begin) != 0 -> depth++
                // Unescaped end delimiter.
                // This leads to the end of the argument if the delimiters are balanced.
                unescapedMatch(string, x, end) != 0 -> {
                    if (depth == 0) {
                        return x - position
                    }
                    depth--
                }
            }
        }
        return 0
    }
}
