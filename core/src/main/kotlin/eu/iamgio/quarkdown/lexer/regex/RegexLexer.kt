package eu.iamgio.quarkdown.lexer.regex

import eu.iamgio.quarkdown.lexer.AbstractLexer
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.regex.pattern.groupify

/**
 * A [Lexer] that identifies tokens by matching [Regex] patterns.
 * @param source the content to be tokenized
 * @param patterns the patterns to search for, in descending order of priority
 */
abstract class RegexLexer(
    source: CharSequence,
    protected val patterns: List<TokenRegexPattern>,
) : AbstractLexer(source) {
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

                // The token data.
                val data =
                    TokenData(
                        text = group.value,
                        position = range,
                        groups = result.groups.asSequence().filterNotNull().map { it.value },
                    )

                // Text tokens are substrings that were not captured by any pattern.
                // These uncaptured groups are scanned and converted to tokens.
                pushFillToken(untilIndex = range.first)

                // Lets the corresponding Token subclass wrap the data.
                this += pattern.wrap(data)

                currentIndex = range.last + 1
            }
        }

    override fun tokenize(): List<Token> =
        buildList {
            currentIndex = 0

            val regex: Regex = patterns.groupify()
            // Append an empty line to the tokenized source to prevent issues with some expressions.
            val match: Sequence<MatchResult> = regex.findAll("$source\n")

            match.forEach { result ->
                addAll(extractMatchingTokens(result))
            }

            // Add a token to fill the gap between the last token and the EOF.
            pushFillToken(untilIndex = source.length)
        }

    /**
     * @param position range of the uncaptured group
     * @return a new token that represents the uncaptured content in order to fill the gaps, or `null` to not fill gaps
     */
    abstract fun createFillToken(position: IntRange): Token?
}
