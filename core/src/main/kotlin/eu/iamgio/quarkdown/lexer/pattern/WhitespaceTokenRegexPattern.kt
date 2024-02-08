package eu.iamgio.quarkdown.lexer.pattern

import eu.iamgio.quarkdown.lexer.type.TokenType
import eu.iamgio.quarkdown.lexer.type.WhitespaceTokenType

/**
 * Collection of [TokenRegexPattern]s that match whitespaces.
 */
enum class WhitespaceTokenRegexPattern(override val tokenType: TokenType, override val regex: Regex) : TokenRegexPattern {
    // Note: names must not contain underscores to prevent issues with RegEx named groups.
    // The order of the entries is relevant to determine match priorities.

    /**
     * Two spaces or a tabulation at the end of the line.
     */
    TRAILINGINDENT(WhitespaceTokenType.TRAILING_INDENT, "( {2,}|\\t)+(?=$)".toRegex()),

    /**
     * Two spaces or a tabulation (or more) in the middle of a line.
     */
    MIDDLEWHITESPACE(WhitespaceTokenType.MIDDLE_WHITESPACE, "(?<!^| |\\t)( {2,}|\\t)+(?!\$| |\\t)".toRegex()),

    /**
     * Four spaces or a tabulation at the beginning of the line.
     */
    LEADINGINDENT(WhitespaceTokenType.LEADING_INDENT, "(?<=^| {4}|\\t)( {4}|\\t)".toRegex()),

    /**
     * End of line.
     */
    EOL(WhitespaceTokenType.EOL, "$(\\R)?".toRegex()),
}
