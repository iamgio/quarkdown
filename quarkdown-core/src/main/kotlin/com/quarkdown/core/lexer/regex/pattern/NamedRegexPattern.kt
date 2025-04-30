package com.quarkdown.core.lexer.regex.pattern

/**
 * A regex pattern with a name, that can be used with [groupify] to group multiple patterns into a bigger pattern.
 */
interface NamedRegexPattern {
    /**
     * Name of the pattern, used to identify the capture group.
     * Should not include special characters and must be unique.
     */
    val name: String

    /**
     * The regex pattern.
     */
    val regex: Regex
}
