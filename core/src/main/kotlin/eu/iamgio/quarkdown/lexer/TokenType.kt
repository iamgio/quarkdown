package eu.iamgio.quarkdown.lexer

/**
 * Possible types of [Token]s.
 */
enum class TokenType {
    /**
     * Beginning of a title.
     * i.e. the `##` in `## Title`
     */
    HEADING,

    /**
     * End of a title, for visuals only.
     * i.e. the `####` in `## Title ####`
     */
    HEADING_CLOSE,

    /**
     * Plain textual content.
     */
    TEXT,

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
    ;

    /**
     * @return whether this type represents a whitespace
     */
    fun isWhitespace() = this == LEADING_INDENT || this == TRAILING_INDENT || this == MIDDLE_WHITESPACE || this == EOL
}
