package eu.iamgio.quarkdown.lexer

/**
 * [Regex] patterns that capture their corresponding [TokenType] within a raw string.
 * @param tokenType type of the token this pattern captures
 * @param regex [Regex] pattern to match
 */
enum class TokenTypePattern(val tokenType: TokenType, val regex: Regex) {
    // Note: names must not contain underscores to prevent issues with RegEx named groups.

    /**
     * Two spaces or a tabulation at the beginning of the line.
     */
    LEADINGINDENT(TokenType.LEADING_INDENT, "((?<=^| {2}|\\t)( {2}|\\t))".toRegex()),

    /**
     * Two spaces or a tabulation at the end of the line.
     */
    TRAILINGINDENT(TokenType.TRAILING_INDENT, "( {2}|\\t)(?=$| {2}|\\t)".toRegex()),

    /**
     * End of line.
     */
    EOL(TokenType.EOL, "$".toRegex()),

    /**
     * Text content, alias anything that was previously unmatched.
     */
    TEXT(TokenType.TEXT, ".+".toRegex()),
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
                .toRegex(RegexOption.MULTILINE)
    }
}
