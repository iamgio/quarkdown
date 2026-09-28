package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.block.BlockEnd
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.extendWhile
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.tokens.NewlineToken

/**
 * Recognizes a run of blank lines. The token's text spans every blank line with its terminator, so
 * consecutive blank lines collapse into a single token.
 */
object NewlineRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        if (!cursor.current.isBlank) return null
        val extent = cursor.extendWhile(end = BlockEnd.AFTER_TERMINATOR) { it.current.isBlank }
        return cursor.match(extent, wrap = ::NewlineToken)
    }
}
