package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.scan.ESCAPE
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.tokens.EscapeToken

private const val ESCAPABLE = "!\"#$%&'()*+,-./:;<=>?@[]\\^_`{|}~"

/**
 * Recognizes a backslash followed by an ASCII punctuation character.
 */
object EscapeRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (source.getOrNull(index) != ESCAPE) return null
        val scanner = Scanner(source, index = index)
        scanner.take(ESCAPE)
        val escaped = scanner.peek() ?: return null
        if (!scanner.takeIf { it in ESCAPABLE }) return null
        return inlineMatch(source, index, scanner.index) { EscapeToken(it, character = escaped.toString()) }
    }
}
