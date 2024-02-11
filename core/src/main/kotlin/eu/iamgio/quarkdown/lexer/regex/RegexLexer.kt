package eu.iamgio.quarkdown.lexer.regex

import eu.iamgio.quarkdown.lexer.AbstractLexer
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 * A [Lexer] that identifies tokens by matching [Regex] patterns.
 * @param source the content to be tokenized
 * @param patterns the patterns to search for, in descending order of priority
 */
abstract class RegexLexer(source: CharSequence, private val patterns: List<TokenRegexPattern>) : AbstractLexer(source) {
    /**
     * Adds a token to fill the gap between the last matched index and [untilIndex], if there is any.
     * @param untilIndex end of the gap range
     * @see createFillToken
     */
    private fun MutableList<Token>.pushFillToken(untilIndex: Int) {
        if (untilIndex > currentIndex) {
            createFillToken(position = currentIndex until untilIndex)?.let { this += it }
        }
    }

    /**
     * Converts a capture of a [Regex] match to a sequence of tokens.
     * @param result result of the [Regex] match
     * @param pattern the pattern used to retrieve the result
     * @return a new token that matches the result
     */
    private fun extractToken(
        result: MatchResult,
        pattern: TokenRegexPattern,
    ) = Token(
        type = pattern.tokenType,
        text = result.value,
        // TODO
        literal = null,
        position = result.range,
    )

    override fun tokenize(): List<Token> =
        buildList {
            currentIndex = 0

            while (currentIndex <= source.length) {
                for (pattern in patterns) {
                    val result = pattern.regex.find(source, startIndex = currentIndex) ?: continue
                    if (result.range.first != currentIndex) continue

                    // Match found
                    this += extractToken(result, pattern)
                    currentIndex = result.range.last + 1
                    break
                }

                currentIndex++
            }

            // Add a token to fill the gap between the last token and the EOF.
            // pushFillToken(untilIndex = source.length)
        }.let { manipulate(it) }

    /**
     * @param position range of the uncaptured group
     * @return a new token that represents the uncaptured content in order to fill the gaps, or `null` to not fill gaps
     */
    abstract fun createFillToken(position: IntRange): Token?

    /**
     * Performs operations on the final (post-tokenization) list of [tokens].
     * @param tokens unprocessed tokenization output
     * @return processed tokenization output
     */
    abstract fun manipulate(tokens: List<Token>): List<Token>
}
