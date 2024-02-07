package eu.iamgio.quarkdown.lexer

/**
 * [Regex] patterns that capture their corresponding [TokenType] within a raw string.
 * @param tokenType type of the token this pattern captures
 * @param regex [Regex] pattern to match
 */
enum class TokenTypePattern(val tokenType: TokenType, val regex: Regex) {
    // Note: names must not contain underscores to prevent issues with RegEx named groups.

    /**
     * Four spaces or a tabulation at the beginning of the line.
     */
    LEADINGINDENT(TokenType.LEADING_INDENT, "(?<=^)( {4}|\\t)+".toRegex()),

    /**
     * Two spaces or a tabulation at the end of the line.
     */
    TRAILINGINDENT(TokenType.TRAILING_INDENT, "( {2,}|\\t)+(?=$)".toRegex()),

    /**
     * Two spacer or a tabulation (or more) in middle of a line.
     */
    MIDDLEWHITESPACE(TokenType.MIDDLE_WHITESPACE, "( {2,}|\\t)+".toRegex()),

    /**
     * End of line.
     */
    EOL(TokenType.EOL, "$(\\R)?".toRegex()),
    ;

    companion object {
        /**
         * Groups all entries of [TokenTypePattern] into a single [Regex] where every capture group is identified by its [tokenType].
         * @return a single [Regex] that captures all groups.
         */
        fun groupify(): Regex =
            values().asSequence()
                .map { pattern ->
                    "(?<${pattern.name}>${pattern.regex})"
                }
                .joinToString(separator = "|")
                .also { println(it) }
                .toRegex(RegexOption.MULTILINE)
    }
}
