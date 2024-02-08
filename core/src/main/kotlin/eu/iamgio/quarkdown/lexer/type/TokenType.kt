package eu.iamgio.quarkdown.lexer.type

/**
 * A type of token.
 */
interface TokenType {
    /**
     * Name of the token.
     */
    val name: String

    /**
     * @return whether this type represents a whitespace
     */
    fun isWhitespace() = this is WhitespaceTokenType
}
