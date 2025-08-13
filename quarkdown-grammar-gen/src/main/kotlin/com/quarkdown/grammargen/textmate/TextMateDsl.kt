package com.quarkdown.grammargen.textmate

import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern

private fun TokenRegexPattern.textMate(
    scope: String,
    type: TextMatePatternType?,
): TextMatePattern =
    TextMatePattern(
        name = this.name,
        pattern = this,
        type = type,
        scope = scope,
    )

internal infix fun TokenRegexPattern.textMateBlock(scope: String): TextMatePattern = this.textMate(scope, TextMatePatternType.BLOCK)

internal infix fun TokenRegexPattern.textMateInline(scope: String): TextMatePattern = this.textMate(scope, TextMatePatternType.INLINE)

internal infix fun TextMatePattern.capturing(capture: Pair<Int, String>): TextMatePattern =
    this.copy(captures = this.captures + PatternCapture(capture.first, capture.second))

internal infix fun TextMatePattern.including(type: TextMatePatternType): TextMatePattern = this.copy(includes = this.includes + type)

internal val blocks = TextMatePatternType.BLOCK
internal val inlines = TextMatePatternType.INLINE
