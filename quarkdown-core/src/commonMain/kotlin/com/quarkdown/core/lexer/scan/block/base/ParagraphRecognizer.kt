package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.Interruption
import com.quarkdown.core.lexer.scan.block.extendWhile
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.tokens.ParagraphToken

/**
 * Recognizes plain text content: a non-empty line, continued by every following line that is neither empty
 * nor an interruption.
 *
 * The paragraph's text stops before the terminator of its last line, and the block lexer drops that newline.
 *
 * @param interruption what ends the paragraph, supplied by the flavor
 */
class ParagraphRecognizer(
    private val interruption: Interruption,
) : BlockRecognizer {
    override val isLineAnchored = false

    override fun open(cursor: LineCursor): BlockMatch? {
        if (cursor.current.isEmpty) return null
        val extent = cursor.extendWhile { !it.current.isEmpty && !interruption.interrupts(it) }
        return cursor.match(extent, wrap = ::ParagraphToken)
    }
}
