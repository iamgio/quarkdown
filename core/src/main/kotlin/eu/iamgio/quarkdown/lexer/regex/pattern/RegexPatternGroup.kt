package eu.iamgio.quarkdown.lexer.regex.pattern

/**
 * Groups a sequence of patterns into a single [Regex] where every capture group is identified by its token type.
 * @return a single [Regex] that captures all groups.
 */
fun Iterable<TokenRegexPattern>.groupify(): Regex =
    this.asSequence()
        .map { pattern ->
            "(?<${pattern.name}>${pattern.regex})"
        }
        .joinToString(separator = "|")
        .toRegex(setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
