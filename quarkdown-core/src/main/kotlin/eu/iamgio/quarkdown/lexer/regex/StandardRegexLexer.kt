package eu.iamgio.quarkdown.lexer.regex

import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 * A standard [RegexLexer] implementation that does not perform any manipulation on the final output and has an optional fixed fill token.
 * @param source the content to be tokenized
 * @param patterns the patterns to search for, in descending order of priority
 * @param fillTokenType type of token to assign to uncaptured groups in order to fill the gaps. A token will not be created if `null` and empty gaps will be present in the output
 */
class StandardRegexLexer(
    source: CharSequence,
    patterns: List<TokenRegexPattern>,
    private val fillTokenType: ((TokenData) -> Token)? = null,
) : RegexLexer(source, patterns) {
    override fun createFillToken(position: IntRange): Token? {
        if (fillTokenType == null) {
            return null
        }

        val text = source.substring(position)

        return TokenData(
            text,
            position,
        ).let { fillTokenType.invoke(it) }
    }

    /**
     * Creates a copy of this lexer with different patterns.
     * @param newPatterns supplier of the new patterns, with the current ones as arguments
     * @return a new instance of [StandardRegexLexer].
     */
    fun updatePatterns(newPatterns: (List<TokenRegexPattern>) -> (List<TokenRegexPattern>)) =
        StandardRegexLexer(
            source,
            newPatterns(super.patterns),
            fillTokenType,
        )
}
