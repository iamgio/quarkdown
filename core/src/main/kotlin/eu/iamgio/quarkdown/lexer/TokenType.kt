package eu.iamgio.quarkdown.lexer

/**
 * Possible types of [Token]s.
 */
enum class TokenType {
    /**
     * Beginning of a title.
     * i.e. the `##` in `## Title`
     */
    HEADING_BEGIN,

    /**
     * End of a title.
     * i.e. the `####` in `## Title ####`
     */
    HEADING_END,

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
}
