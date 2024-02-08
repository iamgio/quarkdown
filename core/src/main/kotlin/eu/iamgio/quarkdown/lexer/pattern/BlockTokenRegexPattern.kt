package eu.iamgio.quarkdown.lexer.pattern

import eu.iamgio.quarkdown.lexer.type.TokenType

/**
 * Collection of [TokenRegexPattern]s that match macro-blocks.
 */
enum class BlockTokenRegexPattern(override val tokenType: TokenType, override val regex: Regex) : TokenRegexPattern {
}
