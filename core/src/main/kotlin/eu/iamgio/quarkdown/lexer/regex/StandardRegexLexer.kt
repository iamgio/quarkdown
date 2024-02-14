package eu.iamgio.quarkdown.lexer.regex

import eu.iamgio.quarkdown.lexer.RawToken
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.type.Token

/**
 * A standard [RegexLexer] implementation that does not perform any manipulation on the final output and has an optional fixed fill token.
 * @param source the content to be tokenized
 * @param patterns the patterns to search for, in descending order of priority
 * @param fillTokenType type of token to assign to uncaptured groups in order to fill the gaps. A token will not be created if `null` and empty gaps will be present in the output
 */
open class StandardRegexLexer(
    source: CharSequence,
    patterns: List<TokenRegexPattern>,
    private val fillTokenType: Token? = null,
) : RegexLexer(source, patterns) {
    override fun createFillToken(position: IntRange): RawToken? {
        if (fillTokenType == null) {
            return null
        }

        val text = source.substring(position)

        return RawToken(
            fillTokenType,
            text,
            position,
        )
    }

    override fun manipulate(tokens: List<RawToken>) = tokens
}
