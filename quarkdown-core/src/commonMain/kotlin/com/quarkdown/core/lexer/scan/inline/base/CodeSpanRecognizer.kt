package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.scan.BACKTICK
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.tokens.CodeSpanToken

/**
 * Recognizes an inline fragment of code.
 * The opening and closing runs must have the same length, and neither may be extended by a further backtick,
 * which is what lets a longer run wrap a shorter one.
 */
object CodeSpanRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (source.getOrNull(index) != BACKTICK) return null
        val scanner = Scanner(source, index = index)
        if (scanner.previous() == BACKTICK) return null
        val openingLength = scanner.takeRun(BACKTICK)
        if (openingLength == 0) return null
        val contentStart = scanner.mark()
        while (!scanner.isAtEnd) {
            scanner.takeWhile { it != BACKTICK }
            val closingStart = scanner.mark()
            val run = scanner.takeRun(BACKTICK)
            if (run == 0) break
            if (run == openingLength && closingStart > contentStart) {
                val content = source.substring(contentStart, closingStart)
                return inlineMatch(source, index, scanner.index) { CodeSpanToken(it, content) }
            }
        }
        return null
    }
}
