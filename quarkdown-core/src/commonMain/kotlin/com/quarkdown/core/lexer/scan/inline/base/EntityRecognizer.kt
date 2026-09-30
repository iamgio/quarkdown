package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.tokens.EntityToken

private const val ENTITY_BEGIN = '&'

private const val ENTITY_END = ';'

private const val NUMERIC_MARKER = '#'

private const val HEXADECIMAL_MARKER = "x"

/**
 * Recognizes a character entity.
 * The hexadecimal marker accepts either case, and the closing semicolon is optional.
 */
object EntityRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (source.getOrNull(index) != ENTITY_BEGIN) return null
        val scanner = Scanner(source, index = index)
        scanner.take(ENTITY_BEGIN)
        val bodyStart = scanner.mark()
        var numeric: String? = null
        if (scanner.take(NUMERIC_MARKER)) {
            val isHexadecimal = scanner.take(HEXADECIMAL_MARKER, ignoreCase = true)
            val numberStart = scanner.mark()
            if (scanner.takeWhile { it.isDigitOf(isHexadecimal) } == 0) return null
            numeric = source.substring(numberStart, scanner.index)
        } else if (scanner.takeWhile { it.isLetterOrDigit() || it == '_' } == 0) {
            return null
        }
        val body = scanner.sliceFrom(bodyStart)
        scanner.take(ENTITY_END)
        return inlineMatch(source, index, scanner.index) { EntityToken(it, body = body, numeric = numeric) }
    }
}

private fun Char.isDigitOf(isHexadecimal: Boolean): Boolean = if (isHexadecimal) isDigit() || lowercaseChar() in 'a'..'f' else isDigit()
