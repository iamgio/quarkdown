package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.commentEndAt
import com.quarkdown.core.lexer.tokens.CommentToken

/**
 * Recognizes a comment, at a line start or in the middle of a line the scan resumed inside.
 * A comment may span lines and absorbs nothing after its closing delimiter, so its text is not line-aligned
 * and its match carries an explicit end index.
 */
object CommentRecognizer : BlockRecognizer {
    override val isLineAnchored = false

    override fun open(cursor: LineCursor): BlockMatch? {
        val line = cursor.current
        val end = commentEndAt(line.sourceOf(), line.start) ?: return null
        val lineCount = cursor.lineCountThrough(end) ?: return null
        return cursor.match(lineCount, endIndex = end, wrap = ::CommentToken)
    }
}
