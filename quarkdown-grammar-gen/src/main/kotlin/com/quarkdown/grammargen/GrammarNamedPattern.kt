package com.quarkdown.grammargen

import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern

/**
 * Represents a named pattern used in grammar generation.
 */
interface GrammarNamedPattern {
    /**
     * The unique name of the pattern.
     */
    val name: String

    /**
     * The regex pattern associated with this named pattern.
     */
    val pattern: TokenRegexPattern
}
