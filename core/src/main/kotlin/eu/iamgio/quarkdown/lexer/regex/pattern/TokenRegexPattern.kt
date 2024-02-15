package eu.iamgio.quarkdown.lexer.regex.pattern

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenWrapper

/**
 * A [Regex] pattern that captures a corresponding [Node] from a raw string.
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
    val tokenWrapper: (Token) -> TokenWrapper

    /**
     * Regex pattern to match.
     */
    val regex: Regex
}
