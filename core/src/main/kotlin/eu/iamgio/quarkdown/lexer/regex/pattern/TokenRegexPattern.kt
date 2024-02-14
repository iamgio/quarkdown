package eu.iamgio.quarkdown.lexer.regex.pattern

import eu.iamgio.quarkdown.lexer.type.Token

/**
 * A [Regex] pattern that captures a corresponding [Token] from a raw string.
 */
interface TokenRegexPattern {
    /**
     * Name of this pattern.
     * A name should not contain special characters (including underscores) to prevent Regex compilation errors.
     */
    val name: String

    /**
     * Type of token this pattern captures.
     */
    val tokenType: Token

    /**
     * Regex pattern to match.
     */
    val regex: Regex
}
