package com.quarkdown.core.lexer.regex.pattern

import java.util.concurrent.ConcurrentHashMap

/**
 * Cache for compiled [Regex] patterns, keyed by their joined string representation.
 * Avoids recompiling the same regex patterns on every lexer instantiation.
 */
private val groupifyCache = ConcurrentHashMap<String, Regex>()

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
