package eu.iamgio.quarkdown.lexer.regex

import eu.iamgio.quarkdown.lexer.AbstractLexer
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.regex.pattern.groupify
import eu.iamgio.quarkdown.lexer.walker.WalkerLexer
import eu.iamgio.quarkdown.util.filterNotNullValues
import eu.iamgio.quarkdown.util.normalizeLineSeparators

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
     * This happens when a matched patterns require a [WalkerLexer] to scan further content.
     */
    private fun MutableList<Token>.extractMatchingTokens(result: MatchResult): Boolean {
        patterns.forEach { pattern ->
            val group = result.groups[pattern.name] ?: return@forEach
            val range = group.range

            // Groups with a name, defined by the pattern.
            val namedGroups =
                pattern.groupNames.asSequence()
                    .map { it to result.groups[it] }
                    .filterNotNullValues()
                    .toMap()

            // Regular groups that are not named.
            // They don't contain values from namedGroups
            val groups =
                result.groups.asSequence()
                    .filterNotNull()
                    // Named groups don't appear in regular groups
                    .filterNot { namedGroups.containsValue(it) }
                    .map { it.value }
                    .toMutableList()

            // namedGroups as Map<String, String>, which can be affected by
            // the output of the walker lexer (see below).
            val namedGroupsValues =
                namedGroups
                    .mapValues { (_, group) -> group.value }
                    .toMutableMap()

            // Fill-tokens are substrings that were not captured by any pattern.
            // These uncaptured groups are scanned and converted to tokens.
            pushFillToken(untilIndex = range.first)

            // End of the match
            currentIndex = range.last + 1

            // Text of the token.
            val text = StringBuilder(group.value)

            // In case the pattern requires additional information that can't be supplied by regex,
            // its WalkerLexer implementation is retrieved and starts scanning from this position.
            // Its produced tokens are stored into the main token's groups.
            pattern.walker?.invoke(source.substring(currentIndex))?.let { walker ->
                // Results are stored as tokens.
                val walkedTokens = walker.tokenize()

                walkedTokens.forEach { token ->
                    // Text of the group.
                    val groupText = token.data.text
                    text.append(groupText)

                    // Named tokens are saved as named groups.
                    // Other tokens are saved as regular groups.
                    if (token is NamedToken) {
                        namedGroupsValues[token.name] = groupText
                    } else {
                        groups += groupText
                    }
                }

                // The matching process is continued from the walker's end position.
                currentIndex += walker.currentIndex
            }

            // The token data.
            val data =
                TokenData(
                    text = text.toString(),
                    position = range,
                    groups = groups.asSequence(),
                    namedGroups = namedGroupsValues,
                )

            // Lets the corresponding Token subclass wrap the data.
            this += pattern.wrap(data)

            // If the pattern has used a walker to scan content, the regex tokenization process must be restarted,
            // and it will start matching regexes again from currentIndex.
            if (pattern.walker != null) {
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
