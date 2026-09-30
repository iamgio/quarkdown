package com.quarkdown.core.lexer.scan.inline.quarkdown

import com.quarkdown.core.lexer.scan.CharCategories
import com.quarkdown.core.lexer.scan.MATH_DELIMITER
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.scan.takeOnelineMath
import com.quarkdown.core.lexer.tokens.InlineMathToken

/**
 * Recognizes a one-line math expression inside a line of text.
 * The expression must be surrounded by a line start, whitespace or a non-word character on both sides. A
 * closing delimiter that is not followed by such a boundary is skipped and a later candidate is tried.
 */
object InlineMathRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (source.getOrNull(index) != MATH_DELIMITER) return null
        val scanner = Scanner(source, index = index)
        if (!isBoundary(scanner.previous())) return null
        val expression = scanner.takeOnelineMath { isBoundary(peek()) } ?: return null
        return inlineMatch(source, index, scanner.index) { InlineMathToken(it, expression = expression) }
    }
}

/**
 * @param char the neighboring character, `null` at the edges of the source
 * @return whether the position is a valid boundary
 */
private fun isBoundary(char: Char?): Boolean =
    char == null || CharCategories.isAsciiWhitespace(char) || !(char.isLetterOrDigit() || char == '_')
