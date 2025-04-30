package com.quarkdown.core.lexer.regex.pattern

/**
 * Groups a sequence of patterns into a single [Regex] where every capture group is identified by its token type (name).
 * @return a single [Regex] that captures all groups.
 */
fun Iterable<NamedRegexPattern>.groupify(): Regex =
    this.joinToString(separator = "|") { pattern ->
        "(?<${pattern.name}>${pattern.regex})"
    }.toRegex(setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
