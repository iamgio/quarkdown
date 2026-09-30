package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.Indentation
import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.TAB_WIDTH
import com.quarkdown.core.lexer.scan.block.BlockEnd
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.extent
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.scanner
import com.quarkdown.core.lexer.tokens.BlockCodeToken

/**
 * Recognizes content indented by at least four columns.
 *
 * Blank lines belong to the block when another code line follows them. When the block ends, it takes the
 * terminator of its last code line plus the blank run that follows, and that run accepts spaces and newlines
 * only, never tabs.
 */
object IndentedCodeRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        if (!cursor.current.isCodeLine()) return null
        var lineCount = 1
        while (true) {
            var blanks = 0
            while (cursor.peek(lineCount + blanks)?.isBlank == true) blanks++
            if (cursor.peek(lineCount + blanks)?.isCodeLine() != true) break
            lineCount += blanks + 1
        }
        var trailing = 0
        while (cursor.peek(lineCount + trailing)?.isSpacesOrEmpty() == true) trailing++
        val content =
            buildString {
                for (offset in 0 until lineCount) {
                    if (offset > 0) append('\n')
                    cursor.peek(offset)!!.appendWithoutCodeIndent(this)
                }
                repeat(trailing + 1) { append('\n') }
            }
        return cursor.match(cursor.extent(lineCount + trailing, BlockEnd.AFTER_TERMINATOR)) { BlockCodeToken(it, content) }
    }
}

/**
 * @return whether the line is indented by at least four columns and holds non-whitespace content
 */
private fun Line.isCodeLine(): Boolean = indent >= TAB_WIDTH && !isBlank

/**
 * @return whether the line holds only spaces, or nothing at all
 */
private fun Line.isSpacesOrEmpty(): Boolean =
    scanner().run {
        takeWhile { it == ' ' }
        isAtEnd
    }

/**
 * Four *columns* of indentation are the marker, the same currency the block is recognized in, so a
 * tab-indented line loses its tab.
 * @return the line without its code indentation
 */
private fun Line.appendWithoutCodeIndent(builder: StringBuilder) {
    builder.append(sourceOf(), Indentation.endOfColumns(sourceOf(), start, end, TAB_WIDTH), end)
}
