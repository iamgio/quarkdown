package com.quarkdown.core.lexer.regex.pattern

import com.quarkdown.core.util.ConcurrentCache

/**
 * Cache for compiled [Regex] patterns, keyed by their joined string representation.
 * Avoids recompiling the same regex patterns on every lexer instantiation.
 */
private val groupifyCache = ConcurrentCache<String, Regex>()

/**
 * Groups a sequence of patterns into a single [Regex] where every capture group is identified by its token type (name).
 * The result is cached so that repeated calls with the same patterns reuse the compiled [Regex].
 * @return a single [Regex] that captures all groups.
 */
fun Iterable<NamedRegexPattern>.groupify(): Regex {
    val joined =
        joinToString(separator = "|") { pattern ->
            "(?<${pattern.name}>${pattern.regex})"
        }
    return groupifyCache.getOrPut(joined) {
        joined.toRegex(setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
    }
}

/**
 * @param pattern one of these patterns
 * @return the indices, within a match of the regex produced by [groupify] on these patterns,
 *         of the groups that belong to [pattern]: the named group that wraps it, followed by its own capturing groups
 * @throws IllegalArgumentException if [pattern] is not one of these patterns
 */
fun Iterable<TokenRegexPattern>.groupIndices(pattern: TokenRegexPattern): IntRange {
    var wrappingGroup = 1
    for (other in this) {
        if (other === pattern) return wrappingGroup..wrappingGroup + pattern.capturingGroupCount
        wrappingGroup += 1 + other.capturingGroupCount
    }
    throw IllegalArgumentException("Pattern ${pattern.name} is not part of the group")
}
