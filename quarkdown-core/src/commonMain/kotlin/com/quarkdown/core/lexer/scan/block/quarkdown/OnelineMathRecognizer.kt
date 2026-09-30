package com.quarkdown.core.lexer.scan.block.quarkdown

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.TrailingCustomId
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.extent
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.contentScanner
import com.quarkdown.core.lexer.scan.takeOnelineMath
import com.quarkdown.core.lexer.tokens.OnelineMathToken

/**
 * Recognizes a one-line math block.
 * The whole line must be the expression plus an optional custom ID; anything else falls through to the
 * paragraph, where the inline math recognizer finds it instead.
 */
object OnelineMathRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        val line = cursor.current
        if (line.indent > MAX_BLOCK_INDENT) return null
        val scanner = line.contentScanner()
        var customId: TrailingCustomId? = null
        val expression =
            scanner.takeOnelineMath {
                customId = line.trailingCustomIdOnly(index)
                customId != null
            } ?: return null
        val identifier = customId?.value
        return cursor.match(cursor.extent()) { data ->
            OnelineMathToken(data, expression = expression, customId = identifier)
        }
    }
}
