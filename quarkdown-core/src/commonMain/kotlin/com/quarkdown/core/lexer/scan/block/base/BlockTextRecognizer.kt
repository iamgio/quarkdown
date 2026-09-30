package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.extent
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.tokens.BlockTextToken

/**
 * Recognizes any non-blank line no other recognizer claimed.
 * As the last recognizer of every block lexer, it guarantees the scan always makes progress.
 */
object BlockTextRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        if (cursor.current.isBlank) return null
        return cursor.match(cursor.extent(), wrap = ::BlockTextToken)
    }
}
