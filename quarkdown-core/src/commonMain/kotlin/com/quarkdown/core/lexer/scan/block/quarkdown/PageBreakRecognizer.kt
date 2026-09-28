package com.quarkdown.core.lexer.scan.block.quarkdown

import com.quarkdown.core.lexer.scan.CharCategories
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.contentScanner
import com.quarkdown.core.lexer.tokens.PageBreakToken

private const val PAGE_BREAK_MARKER = '<'

private const val MIN_PAGE_BREAK_LENGTH = 3

/**
 * Recognizes a page break.
 * The token covers the marker alone, so its text is not line-aligned: the whitespace that must follow the
 * marker is checked but not consumed.
 */
object PageBreakRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        val line = cursor.current
        if (line.indent > MAX_BLOCK_INDENT) return null
        val markers = line.countContentRun(PAGE_BREAK_MARKER)
        val isIsolated =
            markers >= MIN_PAGE_BREAK_LENGTH &&
                line.contentScanner().run {
                    takeRun(PAGE_BREAK_MARKER)
                    takeWhile(CharCategories::isAsciiWhitespace)
                    isAtEnd
                }
        if (!isIsolated) return null
        return cursor.match(lineCount = 1, endIndex = line.contentStart + markers, wrap = ::PageBreakToken)
    }
}
