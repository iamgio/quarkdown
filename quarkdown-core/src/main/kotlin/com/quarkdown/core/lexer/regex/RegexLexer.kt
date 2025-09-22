package com.quarkdown.core.lexer.regex

import com.quarkdown.core.lexer.AbstractLexer
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.regex.pattern.groupify
import com.quarkdown.core.parser.walker.WalkerParser
import com.quarkdown.core.parser.walker.WalkerParsingResult
import com.quarkdown.core.util.filterNotNullValues
import com.quarkdown.core.util.normalizeLineSeparators

/**
 * A [Lexer] that identifies tokens by matching [Regex] patterns.
 * @param source the content to be tokenized
 * @param patterns the patterns to search for, in descending order of priority
 */
abstract class RegexLexer(
    source: CharSequence,
    protected val patterns: List<TokenRegexPattern>,
    // Line separators are set to \n to ensure consistent results.
) : AbstractLexer(source.normalizeLineSeparators()) {
    override var currentIndex: Int = 0
        protected set

    /**
     * Converts captured groups of a [Regex] match to a sequence of tokens, and appends it to [this] list.
     * Uncaptured parts of the source string are converted into other tokens via [createFillToken].
     * @param result result of the [Regex] match
     * @return whether the tokenization process (regex matching) should be restarted from the current index.
     * This happens when a matched pattern produces a [WalkerParsingResult] that scans further content.
     */
    private fun MutableList<Token>.extractMatchingTokens(result: MatchResult): Boolean {
        for (pattern in patterns) {
            val group = result.groups[pattern.name] ?: continue
            val range = group.range

            // Fill-tokens are substrings that were not captured by any pattern.
            // These uncaptured groups are scanned and converted to tokens.
            pushFillToken(untilIndex = range.first)

            // End of the match
            currentIndex = range.last + 1

            // Text of the token.
            val text = StringBuilder(group.value)

            // In case the pattern requires additional information that can't be supplied by regex,
            // its WalkerParser is supplied and starts scanning from this position.
            // In most cases, a pattern will not have a walker.
            // Currently, only function calls have walkers (see parser.walker.funcall), as regex cannot handle balanced argument delimiters.
            val walker: WalkerParser<*>? = pattern.walker?.invoke(source.substring(currentIndex))
            // Result of the walk.
            val walkerResult: WalkerParsingResult<*>? =
                walker?.parse()?.also {
                    // The matching process is continued from the walker's end position.
                    currentIndex += it.endIndex
                }

            // Groups with a name, defined by the pattern.
            val namedGroups =
                pattern.groupNames
                    .asSequence()
                    .map { it to result.groups[it] }
                    .filterNotNullValues()
                    .toMap()

            // Regular groups that are not named.
            // They don't contain values from namedGroups
            val groups =
                result.groups
                    .asSequence()
                    .filterNotNull()
                    // Named groups don't appear in regular groups.
                    .filterNot { namedGroups.containsValue(it) }
                    .map { it.value }
                    .toMutableList()

            // namedGroups as Map<String, String>, which can be affected by
            // the output of the walker lexer (see below).
            val namedGroupsValues =
                namedGroups
                    .mapValues { (_, group) -> group.value }
                    .toMutableMap()

            // The token data.
            val data =
                TokenData(
                    text = text.toString(),
                    position = range,
                    groups = groups.asSequence(),
                    namedGroups = namedGroupsValues,
                    walkerResult = walkerResult,
                )

            // Lets the corresponding Token subclass wrap the data.
            this += pattern.wrap(data)

            // If the pattern has used a walker to scan content, the regex tokenization process must be restarted,
            // and it will start matching regexes again from currentIndex.
            if (walkerResult != null) {
                return true
            }
        }

        return false
    }

    override fun tokenize(): List<Token> =
        buildList {
            val regex: Regex = patterns.groupify()

            fun extract() {
                // Append an empty line to the tokenized source to prevent issues with some expressions.
                val match: Sequence<MatchResult> = regex.findAll("$source\n", currentIndex)

                match.forEach { result ->
                    // If a restart is required, the matching process is restarted from the current index.
                    val shouldRestart = extractMatchingTokens(result)
                    if (shouldRestart) {
                        extract()
                        return
                    }
                }
            }

            extract()

            // Add a token to fill the gap between the last token and the EOF.
            pushFillToken(untilIndex = source.length)
        }
}
