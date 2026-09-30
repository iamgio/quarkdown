package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.block.BlockEnd
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.extent
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.tokens.HorizontalRuleToken

private const val THEMATIC_BREAK_CHARS = "-_*"

private const val MIN_THEMATIC_BREAK_LENGTH = 3

/**
 * Recognizes a thematic break. It absorbs the empty lines that follow it, but not a whitespace-only line,
 * which ends it.
 */
object ThematicBreakRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        if (!opensAt(cursor.current)) return null
        return cursor.match(cursor.extent(end = BlockEnd.AFTER_EMPTY_LINES), wrap = ::HorizontalRuleToken)
    }

    /**
     * @param line line to inspect
     * @return whether the line holds only three or more of the same break character, spaces and tabs
     */
    fun opensAt(line: Line): Boolean {
        if (line.indent > MAX_BLOCK_INDENT) return false
        val marker = line.charAt(line.contentStart)?.takeIf { it in THEMATIC_BREAK_CHARS } ?: return false
        var markers = 0
        for (index in line.contentStart until line.end) {
            when (line.charAt(index)) {
                marker -> markers++
                ' ', '\t' -> Unit
                else -> return false
            }
        }
        return markers >= MIN_THEMATIC_BREAK_LENGTH
    }
}
