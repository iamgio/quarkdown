package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.block.BlockEnd
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.Interruption
import com.quarkdown.core.lexer.scan.block.extendWhile
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.contentScanner
import com.quarkdown.core.lexer.scan.scanner
import com.quarkdown.core.lexer.tokens.BlockQuoteToken

private const val QUOTE_MARKER = '>'

/**
 * Recognizes a block quote.
 *
 * A quote line always continues the quote. A line that is not a quote line continues it lazily, unless the
 * previous quote line was empty, the line itself is empty, or the line interrupts. The token's text always
 * ends with the terminator of its last line.
 *
 * @param interruption what ends the quote, supplied by the flavor
 */
class BlockQuoteRecognizer(
    private val interruption: Interruption,
) : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        if (!opensAt(cursor.current)) return null
        val extent =
            cursor.extendWhile(end = BlockEnd.AFTER_TERMINATOR) { next ->
                when {
                    opensAt(next.current) -> true
                    next.peek(-1)!!.isEmptyQuoteLine() -> false
                    next.current.isEmpty -> false
                    else -> !interruption.interrupts(next)
                }
            }
        val content =
            buildString {
                for (offset in 0 until extent.lineCount) {
                    if (offset > 0) append('\n')
                    cursor.peek(offset)!!.appendWithoutQuoteMarker(this)
                }
            }
        return cursor.match(extent) { BlockQuoteToken(it, content) }
    }

    companion object {
        /**
         * @param line line to inspect
         * @return whether the line starts a block quote
         */
        fun opensAt(line: Line): Boolean = line.indent <= MAX_BLOCK_INDENT && line.charAt(line.contentStart) == QUOTE_MARKER
    }
}

/**
 * An empty quote line ends the quote: the next line does not continue it lazily.
 * @return whether the line is a quote marker followed by at most one space
 */
private fun Line.isEmptyQuoteLine(): Boolean {
    if (!BlockQuoteRecognizer.opensAt(this)) return false
    return contentScanner().run {
        take(QUOTE_MARKER)
        take(' ')
        isAtEnd
    }
}

/**
 * Any amount of leading spaces, one quote marker and one optional space or tab are syntax. A lazy
 * continuation line has no marker and is kept as it is.
 * @return the line without its quote marker
 */
private fun Line.appendWithoutQuoteMarker(builder: StringBuilder) {
    val contentStart =
        scanner().run {
            takeWhile { it == ' ' }
            if (!take(QUOTE_MARKER)) return@run start
            takeIf { it == ' ' || it == '\t' }
            index
        }
    builder.append(sourceOf(), contentStart, end)
}
