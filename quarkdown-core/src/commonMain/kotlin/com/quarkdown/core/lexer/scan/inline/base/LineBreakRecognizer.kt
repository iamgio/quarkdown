package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.scan.CharCategories
import com.quarkdown.core.lexer.scan.ESCAPE
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.tokens.LineBreakToken

private const val MIN_HARD_BREAK_SPACES = 2

private val BREAK_STARTS = setOf(' ', ESCAPE, '\n')

/**
 * Recognizes a line break that is not at the end of a paragraph.
 * A break preceded by two or more spaces or by a backslash is hard; any other is soft. A newline followed
 * only by whitespace up to a line end is not a break at all, so the padding newline never is one.
 */
object LineBreakRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (source.getOrNull(index) !in BREAK_STARTS) return null
        val scanner = Scanner(source, index = index)
        val spaces = scanner.takeWhile { it == ' ' }
        if (spaces == 1) return null
        val isHard = spaces >= MIN_HARD_BREAK_SPACES || scanner.take(ESCAPE)
        if (!scanner.take('\n') || scanner.isBlankToLineEnd()) return null
        return inlineMatch(source, index, scanner.index) { LineBreakToken(it, isHard = isHard) }
    }
}

/**
 * @return whether the whitespace run starting here reaches a line end or the end of input
 */
private fun Scanner.isBlankToLineEnd(): Boolean =
    lookahead {
        takeWhile { CharCategories.isAsciiWhitespace(it) && it != '\n' }
        isAtEnd || peek() == '\n'
    }
