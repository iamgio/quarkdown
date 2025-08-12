package com.quarkdown.grammargen.textmate

import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.grammargen.GrammarNamedPattern

/**
 * A named pattern for use in TextMate grammar definitions.
 * @param name the TextMate scope name for the pattern
 * @param pattern the token regex pattern associated with this scope
 */
data class TextMatePattern(
    override val name: String,
    override val pattern: TokenRegexPattern,
) : GrammarNamedPattern

internal infix fun TokenRegexPattern.textMate(name: String): TextMatePattern = TextMatePattern(name, this)
