package eu.iamgio.quarkdown.lexer.regex

import eu.iamgio.quarkdown.lexer.AbstractLexer
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.regex.pattern.groupify
import eu.iamgio.quarkdown.util.filterNotNullValues

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

                // Groups with a name, defined by the pattern.
                val namedGroups =
                    pattern.groupNames.asSequence()
                        .map { it to result.groups[it] }
                        .filterNotNullValues()
                        .toMap()

                // The token data.
                val data =
                    TokenData(
                        text = group.value,
                        position = range,
                        groups =
                            result.groups.asSequence()
                                .filterNotNull()
                                // Named groups don't appear in regular groups
                                .filterNot { it in namedGroups.values }
                                .map { it.value },
                        namedGroups = namedGroups.mapValues { (name, group) -> group.value },
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
            val regex: Regex = patterns.groupify()
            // Append an empty line to the tokenized source to prevent issues with some expressions.
            val match: Sequence<MatchResult> = regex.findAll("$source\n")

            match.forEach { result ->
                addAll(extractMatchingTokens(result))
            }

            // Add a token to fill the gap between the last token and the EOF.
            pushFillToken(untilIndex = source.length)
        }
}
