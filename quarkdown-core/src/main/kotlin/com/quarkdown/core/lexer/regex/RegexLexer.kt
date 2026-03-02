package com.quarkdown.core.lexer.regex

import com.quarkdown.core.lexer.AbstractLexer
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.regex.pattern.groupify
import com.quarkdown.core.util.filterNotNullValues
import com.quarkdown.core.util.normalizeLineSeparators

/**
 * A [Lexer] that identifies tokens by matching [Regex] patterns.
 *
 * Tokenization uses an iterative `find` loop: each iteration calls [Regex.find] starting from
 * [currentIndex], so that walker-driven position advances are respected without restarting
 * the entire regex search.
 *
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
     * Extracts tokens from a single [Regex] match result.
     * Uncaptured parts of the source string preceding the match are converted into fill tokens
     * via [createFillToken].
     *
     * If a pattern's walker rejects the match by returning `null`, this method reverts the scan position
     * and returns an empty list, signaling the caller to retry with the [fallbackPatterns].
     *
     * @param result result of the [Regex] match
     * @param activePatterns the patterns to check against the match result (must correspond to the regex
     *                       that produced [result], so that named group lookups succeed)
     * @return the tokens produced from this match (typically one content token, optionally preceded by a fill token),
     *         or an empty list if the walker rejected the match
     */
    private fun extractMatchingTokens(
        result: MatchResult,
        activePatterns: List<TokenRegexPattern> = patterns,
    ): List<Token> {
        val tokens = mutableListOf<Token>()

        for (pattern in activePatterns) {
            val group = result.groups[pattern.name] ?: continue
            val range = group.range

            // Save current position in case the walker rejects the match.
            val savedIndex = currentIndex

            // Fill-tokens are substrings that were not captured by any pattern.
            // These uncaptured groups are scanned and converted to tokens.
            if (range.first > currentIndex) {
                createFillToken(position = currentIndex until range.first)?.let { tokens += it }
            }

            // End of the match.
            currentIndex = range.last + 1

            // Groups with a name, defined by the pattern.
            val namedGroups =
                pattern.groupNames
                    .asSequence()
                    .map { it to result.groups[it] }
                    .filterNotNullValues()
                    .toMap()

            // Regular groups that are not named.
            // They don't contain values from namedGroups.
            val groups =
                result.groups
                    .asSequence()
                    .filterNotNull()
                    // Named groups don't appear in regular groups.
                    .filterNot { namedGroups.containsValue(it) }
                    .map { it.value }
                    .toMutableList()

            // namedGroups as Map<String, String>.
            val namedGroupsValues =
                namedGroups
                    .mapValues { (_, group) -> group.value }
                    .toMutableMap()

            // The token data.
            val data =
                TokenData(
                    text = group.value,
                    position = range,
                    groups = groups.asSequence(),
                    namedGroups = namedGroupsValues,
                )

            // In case the pattern requires additional information that can't be supplied by regex,
            // its walker is invoked to produce a fully typed token and advance the scan position.
            // In most cases, a pattern will not have a walker.
            // Currently, only function calls have walkers (see parser.walker.funcall),
            // as regex cannot handle balanced argument delimiters.
            val walked = pattern.walker?.invoke(data, source.substring(currentIndex))
            if (walked != null) {
                currentIndex += walked.charsConsumed
                tokens += walked.token
            } else if (pattern.walker != null) {
                // The walker rejected this match (e.g., a block function call pattern determined
                // the content is actually inline-level). Revert position and signal rejection.
                currentIndex = savedIndex
                return emptyList()
            } else {
                // Lets the corresponding Token subclass wrap the data.
                tokens += pattern.wrap(data)
            }
        }

        return tokens
    }

    override fun tokenize(): Sequence<Token> =
        sequence {
            currentIndex = 0
            val regex: Regex = patterns.groupify()
            // Append an empty line to the tokenized source to prevent issues with some expressions.
            val paddedSource = "$source\n"

            while (currentIndex < source.length) {
                val prevIndex = currentIndex
                var result = regex.find(paddedSource, currentIndex) ?: break
                var tokens = extractMatchingTokens(result)

                // If a walker rejected the match, retry with the fallback regex
                // (which excludes walker-based patterns), allowing other patterns
                // (e.g. paragraph) to match at the same position.
                if (tokens.isEmpty() && currentIndex == prevIndex) {
                    val (fallback, fallbackPats) = fallbackPatterns ?: break
                    result = fallback.find(paddedSource, currentIndex) ?: break
                    tokens = extractMatchingTokens(result, fallbackPats)
                }

                yieldAll(tokens)
                // Safety: ensure forward progress to prevent infinite loops on zero-width matches.
                if (currentIndex <= prevIndex) break
            }

            // Add a token to fill the gap between the last token and the EOF.
            if (currentIndex < source.length) {
                createFillToken(position = currentIndex until source.length)?.let { yield(it) }
            }
        }

    /**
     * Pre-compiled fallback regex and its corresponding pattern list, excluding patterns with walkers.
     * Used when a walker rejects a match, allowing other patterns (e.g. paragraph)
     * to match at the same position where the walker-based pattern was rejected.
     *
     * `null` if no patterns have walkers (no fallback is ever needed).
     */
    private val fallbackPatterns: Pair<Regex, List<TokenRegexPattern>>? by lazy {
        val nonWalkerPatterns = patterns.filter { it.walker == null }
        if (nonWalkerPatterns.size == patterns.size) return@lazy null // No walkers exist.
        if (nonWalkerPatterns.isEmpty()) return@lazy null
        nonWalkerPatterns.groupify() to nonWalkerPatterns
    }
}
