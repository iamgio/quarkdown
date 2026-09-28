package com.quarkdown.core.lexer.scan

/**
 * Leaf matchers shared by the scanners and by the better-parse grammars. Each returns how many characters it
 * matches at an index, or zero for no match, which is the signature better-parse's `Token.match` takes.
 */
object Matchers {
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
            string.getOrNull(position) != char -> 0
            string.getOrNull(position - 1) != ESCAPE -> {
                onMatch()
                1
            }

            else -> 0
        }

    /**
     * Matches a balanced sequence delimited by [begin] and [end], ignoring escaped delimiters.
     *
     * Scans forward starting at [position], which must already be past the opening delimiter, and returns the
     * number of characters up to, but not including, the balancing end delimiter. Returns 0 when the
     * delimiters never balance. Nesting is unbounded, and the scan is a single loop, so its stack cost is
     * constant.
     *
     * @param string the source to scan
     * @param position the starting index to scan from
     * @param begin the opening delimiter
     * @param end the closing delimiter
     * @return the length from [position] to the matching end delimiter, or 0 if none
     */
    fun balancedDelimiters(
        string: CharSequence,
        position: Int,
        begin: Char,
        end: Char,
    ): Int {
        var depth = 0
        for (index in position until string.length) {
            when {
                unescapedMatch(string, index, begin) != 0 -> depth++
                unescapedMatch(string, index, end) != 0 -> {
                    if (depth == 0) return index - position
                    depth--
                }
            }
        }
        return 0
    }
}
