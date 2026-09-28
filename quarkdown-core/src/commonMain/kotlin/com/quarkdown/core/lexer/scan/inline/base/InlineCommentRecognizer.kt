package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.scan.commentEndAt
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.tokens.CommentToken

/**
 * Recognizes a comment anywhere in a line. It shares its scanner with the block comment recognizer.
 */
object InlineCommentRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        val end = commentEndAt(source, index) ?: return null
        return inlineMatch(source, index, end, ::CommentToken)
    }
}
