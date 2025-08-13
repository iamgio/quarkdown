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
    val captures: Set<PatternCapture> = emptySet(),
) : GrammarNamedPattern

/**
 * A capture in a TextMate pattern.
 * @param index the index of the capture group in the regex
 * @param name the TextMate scope name for this capture
 */
data class PatternCapture(
    val index: Int,
    val name: String,
)

internal infix fun TokenRegexPattern.textMate(name: String): TextMatePattern = TextMatePattern(name, this)

internal infix fun TextMatePattern.capturing(capture: Pair<Int, String>): TextMatePattern =
    this.copy(captures = this.captures + PatternCapture(capture.first, capture.second))
