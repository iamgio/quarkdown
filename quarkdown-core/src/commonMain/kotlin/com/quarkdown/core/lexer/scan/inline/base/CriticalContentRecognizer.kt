package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.tokens.CriticalContentToken

private const val CRITICAL_CHARS = "&<>\"'"

/**
 * Recognizes a single character that the rendering stage must treat specially.
 * It is the last recognizer of every inline lexer.
 */
object CriticalContentRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        val char = source.getOrNull(index) ?: return null
        if (char !in CRITICAL_CHARS) return null
        return inlineMatch(source, index, index + 1, ::CriticalContentToken)
    }
}
