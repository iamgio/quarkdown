package eu.iamgio.quarkdown.lexer.type

/**
 *
 */
enum class WhitespaceTokenType : TokenType {
    /**
     * Indentation at the beginning of the line.
     */
    LEADING_INDENT,

    /**
     * Indentation at the end of the line.
     */
    TRAILING_INDENT,

    /**
     * Indentation in the middle of a line.
     */
    MIDDLE_WHITESPACE,

    /**
     * End of line.
     */
    EOL,
}
