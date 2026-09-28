package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.block.BlockEnd
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.Interruption
import com.quarkdown.core.lexer.scan.block.extendWhile
import com.quarkdown.core.lexer.scan.block.extent
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.contentScanner
import com.quarkdown.core.lexer.tokens.TableToken

private const val CELL_SEPARATOR = '|'

private const val ALIGNMENT_MARKER = ':'

private const val ALIGNMENT_FILL = '-'

/**
 * Recognizes a GFM table.
 * The rows run from the third line up to the first blank line or interruption, and the trailing empty
 * lines belong to the token. A caption or custom ID in the last row is the parser's business.
 * @param interruption what ends the rows, which excludes tables so a second table does not cut the first
 */
class TableRecognizer(
    private val interruption: Interruption,
) : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        if (!opensAt(cursor)) return null
        val header = cursor.current
        val alignment = cursor.peek(1)!!
        val headerContent = headerContentOf(header)!!
        val rows = cursor.extendWhile(from = 2) { !it.current.isBlank && !interruption.interrupts(it) }
        val rowsStart = cursor.peek(2)?.start ?: alignment.nextStart
        val rowsEnd = if (rows.lineCount == 2) rowsStart else rows.endIndex
        return cursor.match(cursor.extent(rows.lineCount, BlockEnd.AFTER_EMPTY_LINES)) { data ->
            TableToken(
                data,
                header = headerContent,
                alignment = alignment.content,
                rows = header.sourceSlice(minOf(rowsStart, rowsEnd), rowsEnd),
            )
        }
    }

    companion object {
        /**
         * @param cursor line a table might open at
         * @return whether a table opens there
         */
        fun opensAt(cursor: LineCursor): Boolean {
            val header = cursor.current
            if (!header.isTerminated || !header.hasHeaderContent()) return false
            val alignment = cursor.peek(1) ?: return false
            return isAlignmentRow(alignment)
        }

        /**
         * The header's leading indentation is spaces only, never tabs.
         * @param line line to inspect
         * @return the header row's content, or `null` when the line holds none
         */
        fun headerContentOf(line: Line): String? = if (line.hasHeaderContent()) line.sourceSlice(line.headerStart(), line.end) else null

        /**
         * An alignment row is `:?-+:?` cells, separated and optionally surrounded by pipes, and nothing else.
         * @param line line to inspect
         * @return whether the line is a GFM table alignment row
         */
        fun isAlignmentRow(line: Line): Boolean {
            if (line.indent > MAX_BLOCK_INDENT || line.isBlank) return false
            return line.contentScanner().run {
                takeOptionalPipe()
                if (!takeAlignmentCell()) return false
                repeatWhile { takeOptionalPipe() && takeAlignmentCell() }
                takeOptionalPipe()
                isAtEnd
            }
        }
    }
}

/**
 * Consumes an optional cell separator and the spaces after it.
 * @return whether a separator was there
 */
private fun Scanner.takeOptionalPipe(): Boolean {
    if (!take(CELL_SEPARATOR)) return false
    takeWhile { it == ' ' }
    return true
}

/**
 * Consumes one `:?-+:?` alignment cell and the spaces after it. An alignment row admits spaces only, never
 * tabs.
 * @return whether a cell was there
 */
private fun Scanner.takeAlignmentCell(): Boolean =
    step {
        take(ALIGNMENT_MARKER)
        val dashes = takeRun(ALIGNMENT_FILL)
        take(ALIGNMENT_MARKER)
        takeWhile { it == ' ' }
        dashes > 0
    }

/**
 * The header's leading indentation is spaces only, never tabs.
 * @return index the header row's content starts at
 */
private fun Line.headerStart(): Int {
    var index = start
    while (index < end && charAt(index) == ' ') index++
    return index
}

/**
 * @return whether the line holds anything past its leading spaces
 */
private fun Line.hasHeaderContent(): Boolean = headerStart() < end
