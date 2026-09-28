package com.quarkdown.core.lexer.scan.block.quarkdown

import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.functionCallStartAt
import com.quarkdown.core.lexer.scan.walkFunctionCallAt
import com.quarkdown.core.lexer.scan.walkerSlice
import com.quarkdown.core.lexer.tokens.FunctionCallToken

/**
 * Recognizes an isolated function call.
 *
 * The produced token is zero-width: its text is empty and its position is an empty range, because the call's
 * real extent comes from the walker and the parser derives the source range from it. A call is block-level
 * only when nothing but whitespace follows it on its last line; otherwise this recognizer declines and the
 * line becomes a paragraph.
 */
object FunctionCallRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        val line = cursor.current
        if (line.indent > MAX_BLOCK_INDENT) return null
        val start = line.contentStart
        val source = line.sourceOf()
        if (!functionCallStartAt(source, start)) return null
        val result = walkFunctionCallAt(source, start) ?: return null
        val remaining = source.walkerSlice(start)
        val scanEnd = start + result.endIndex
        if (!remaining.isBlankAfter(result.endIndex)) return null
        val token = FunctionCallToken(TokenData(text = "", position = start until start), isBlock = true, walkerResult = result)
        val lineCount = cursor.lineCountThrough(scanEnd) ?: return null
        return BlockMatch(token, lineCount, scanEnd)
    }
}

/**
 * @param endIndex index the walker stopped at
 * @return whether the call ends cleanly, with no trailing content on the line it ends on
 */
private fun CharSequence.isBlankAfter(endIndex: Int): Boolean {
    if (endIndex >= length || this[endIndex - 1] == '\n') return true
    val lineEnd = indexOf('\n', endIndex).let { if (it < 0) length else it }
    return subSequence(endIndex, lineEnd).isBlank()
}
