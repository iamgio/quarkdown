package eu.iamgio.quarkdown.lexer

/**
 * Possible types of [Token]s.
 */
enum class TokenType {
    /**
     * Indentation at the beginning of the line.
     */
    LEADING_INDENT,

    /**
     * Indentation at the end of the line.
     */
    TRAILING_INDENT,

    /**
     * End of line.
     */
    EOL,
}
