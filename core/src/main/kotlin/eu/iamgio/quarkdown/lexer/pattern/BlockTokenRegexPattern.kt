package eu.iamgio.quarkdown.lexer.pattern

import eu.iamgio.quarkdown.lexer.type.BlockTokenType
import eu.iamgio.quarkdown.lexer.type.TokenType

/**
 * Collection of [TokenRegexPattern]s that match macro-blocks.
 */
enum class BlockTokenRegexPattern(override val tokenType: TokenType, override val regex: Regex) : TokenRegexPattern {
    HEADING(BlockTokenType.HEADING, "(?<=^) {0,3}#{1,6}(.+)?".toRegex())
}
