package eu.iamgio.quarkdown.lexer.regex.pattern

import eu.iamgio.quarkdown.lexer.RawToken
import eu.iamgio.quarkdown.lexer.RawTokenWrapper
import eu.iamgio.quarkdown.lexer.Token

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
     * A function that wraps a general token into its specific wrapper.
     */
    val tokenWrapper: (RawToken) -> RawTokenWrapper

    /**
     * Regex pattern to match.
     */
    val regex: Regex
}
