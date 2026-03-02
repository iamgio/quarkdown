package com.quarkdown.core.lexer.regex.pattern

import com.quarkdown.core.ast.Node
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData

/**
 * A [Regex] pattern that captures a corresponding [Node] from a raw string.
 * @param name name of this pattern.
 *             A name should not contain special characters (including underscores)
 *             in order to prevent Regex compilation errors
 * @param wrap a function that wraps a general token into its specific wrapper
 * @param regex regex pattern to match
 * @param groupNames names of the named groups that appear the regex pattern
 * @param walker if present, upon being captured, receives the [TokenData] from the regex match and the remaining
 *               source after the match, then produces a [WalkedToken] containing a fully typed token and the number
 *               of additional characters consumed, or `null` to reject the match (e.g., when a block function call
 *               determines it is actually inline-level content).
 *               Used when regex alone cannot capture complex tokens (e.g., balanced delimiters in function call arguments)
 */
data class TokenRegexPattern(
    override val name: String,
    val wrap: (TokenData) -> Token,
    override val regex: Regex,
    val groupNames: List<String> = emptyList(),
    val walker: ((TokenData, CharSequence) -> WalkedToken?)? = null,
) : NamedRegexPattern
