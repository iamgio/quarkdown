package com.quarkdown.core.lexer.regex.pattern

import com.quarkdown.core.ast.Node
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.parser.walker.WalkerParser

/**
 * A [Regex] pattern that captures a corresponding [Node] from a raw string.
 * @param name name of this pattern.
 *             A name should not contain special characters (including underscores)
 *             in order to prevent Regex compilation errors
 * @param wrap a function that wraps a general token into its specific wrapper
 * @param regex regex pattern to match
 * @param groupNames names of the named groups that appear the regex pattern
 * @param walker if present, upon being captured, produces a [WalkerParser] that scans the given [CharSequence] source
 *               in case regex does not suffice to capture complex tokens
 */
data class TokenRegexPattern(
    override val name: String,
    val wrap: (TokenData) -> Token,
    override val regex: Regex,
    val groupNames: List<String> = emptyList(),
    val walker: ((CharSequence) -> WalkerParser<*>)? = null,
) : NamedRegexPattern
