package eu.iamgio.quarkdown.lexer.regex.pattern

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.walker.WalkerLexer

/**
 * A [Regex] pattern that captures a corresponding [Node] from a raw string.
 * @param name name of this pattern.
 *             A name should not contain special characters (including underscores)
 *             in order to prevent Regex compilation errors
 * @param wrap a function that wraps a general token into its specific wrapper
 * @param regex regex pattern to match
 * @param groupNames names of the named groups that appear the regex pattern
 * @param walker if present, upon being captured, produces a [WalkerLexer] that scans the given [CharSequence] source
 */
data class TokenRegexPattern(
    val name: String,
    val wrap: (TokenData) -> Token,
    val regex: Regex,
    val groupNames: List<String> = emptyList(),
    val walker: ((CharSequence) -> WalkerLexer)? = null,
)
