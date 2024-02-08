package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.lexer.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.pattern.groupify

/**
 * A [Lexer] that identifies tokens by matching [Regex] patterns.
 * @param source the content to be tokenized
 * @param patterns the patterns to search for, in descending order of priority
 */
abstract class RegexLexer(source: CharSequence, private val patterns: List<TokenRegexPattern>) : AbstractLexer(source) {
    /**
     * Converts captured groups of a [Regex] match to a sequence of tokens.
     * Uncaptured parts of the source string are converted into other tokens via [createFillToken].
     * @param result result of the [Regex] match
     * @return stream of matched tokens
     */
    private fun extractMatchingTokens(result: MatchResult): List<Token> =
        buildList {
            patterns.forEach { pattern ->
                val group = result.groups[pattern.name] ?: return@forEach
                val range = group.range

                // The token itself.
                val token =
                    Token(
                        type = pattern.tokenType,
                        text = group.value,
                        // TODO
                        literal = null,
                        position = range,
                    )

                // Text tokens are substrings that were not captured by any pattern.
                // These uncaptured groups are scanned and converted to tokens.
                if (range.first > currentIndex) {
                    createFillToken(position = currentIndex until range.first)?.let { this += it }
                }

                this += token

                currentIndex = range.last + 1
            }
        }

    override fun tokenize(): List<Token> =
        buildList {
            currentIndex = 0

            val regex: Regex = patterns.groupify()
            val match: Sequence<MatchResult> = regex.findAll(source)

            match.forEach { result ->
                addAll(extractMatchingTokens(result))
            }
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
