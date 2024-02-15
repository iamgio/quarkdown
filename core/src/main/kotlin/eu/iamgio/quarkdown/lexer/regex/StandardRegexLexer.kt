package eu.iamgio.quarkdown.lexer.regex

import eu.iamgio.quarkdown.lexer.RawToken
import eu.iamgio.quarkdown.lexer.RawTokenWrapper
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 * A standard [RegexLexer] implementation that does not perform any manipulation on the final output and has an optional fixed fill token.
 * @param source the content to be tokenized
 * @param patterns the patterns to search for, in descending order of priority
 * @param fillTokenType type of token to assign to uncaptured groups in order to fill the gaps. A token will not be created if `null` and empty gaps will be present in the output
 */
open class StandardRegexLexer(
    source: CharSequence,
    patterns: List<TokenRegexPattern>,
    private val fillTokenType: ((RawToken) -> RawTokenWrapper)? = null,
) : RegexLexer(source, patterns) {
    override fun createFillToken(position: IntRange): RawTokenWrapper? {
        if (fillTokenType == null) {
            return null
        }

        val text = source.substring(position)

        return RawToken(
            text,
            position,
        ).let { fillTokenType.invoke(it) }
    }

    override fun manipulate(tokens: List<RawTokenWrapper>) = tokens
}
