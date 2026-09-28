package com.quarkdown.core.lexer.scan.block.quarkdown

import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MATH_DELIMITER
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.base.MIN_FENCE_LENGTH
import com.quarkdown.core.lexer.scan.block.base.closingFenceStart
import com.quarkdown.core.lexer.scan.block.extendThrough
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.contentScanner
import com.quarkdown.core.lexer.tokens.MultilineMathToken

/**
 * Recognizes a multiline math block fenced by three or more dollar signs.
 * Its closing fence is found by `Line.closingFenceStart`, shared with the fenced code recognizer.
 */
object MultilineMathRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        val opener = cursor.current
        if (opener.indent > MAX_BLOCK_INDENT || opener.countContentRun(MATH_DELIMITER) < MIN_FENCE_LENGTH) return null
        val headerEnd =
            opener.contentScanner().run {
                takeRun(MATH_DELIMITER)
                takeSpacesAndTabs()
                index
            }
        val customId = opener.trailingCustomIdOnly(headerEnd) ?: return null
        val (extent, closing) = cursor.extendThrough { it.closingFenceStart(MATH_DELIMITER) } ?: return null
        val expression = opener.sourceSlice(opener.end, closing)
        return cursor.match(extent) { data ->
            MultilineMathToken(data, expression = expression, customId = customId.value)
        }
    }

    /**
     * An unterminated last line opens nothing, since a fence needs a line of its own.
     * @param line line to inspect
     * @return whether the line opens a multiline math block
     */
    fun opensAt(line: Line): Boolean =
        line.indent <= MAX_BLOCK_INDENT && line.isTerminated && line.countContentRun(MATH_DELIMITER) >= MIN_FENCE_LENGTH
}
