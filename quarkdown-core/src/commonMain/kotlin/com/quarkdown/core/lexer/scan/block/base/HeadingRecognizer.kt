package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.CharCategories
import com.quarkdown.core.lexer.scan.CustomIds
import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.block.BlockEnd
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.extent
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.contentScanner
import com.quarkdown.core.lexer.tokens.HeadingToken

private const val HEADING_MARKER = '#'

private const val DECORATIVE_MARKER = '!'

private const val MAX_HEADING_DEPTH = 6

/**
 * Recognizes a heading opened by one to six `#`.
 * The trailing `#` run and the whitespace before it are dropped, and the heading absorbs every blank line
 * that follows it.
 */
object HeadingRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        val line = cursor.current
        val depth = depthAt(line) ?: return null
        var contentStart = line.contentStart + depth
        val isDecorative = line.charAt(contentStart) == DECORATIVE_MARKER
        if (isDecorative) contentStart++
        val withoutTrailingHashes = line.endWithoutTrailingHashes(contentStart)
        val customId = CustomIds.trailing(line, contentStart, withoutTrailingHashes)
        return cursor.match(cursor.extent(end = BlockEnd.AFTER_BLANK_LINES)) { data ->
            HeadingToken(
                data,
                depth = depth,
                isDecorative = isDecorative,
                content = line.sourceSlice(contentStart, customId.contentEnd),
                customId = customId.value,
            )
        }
    }

    /**
     * A decorative heading is a heading: it opens one and interrupts a paragraph just as a plain one does.
     * @param line line to inspect
     * @return whether the line opens a heading, decorative or not
     */
    fun opensAt(line: Line): Boolean = depthAt(line) != null

    /**
     * @param line line to inspect
     * @return the amount of hashes that open a heading, or `null` when the line opens none
     */
    fun depthAt(line: Line): Int? {
        if (line.indent > MAX_BLOCK_INDENT || line.charAt(line.contentStart) != HEADING_MARKER) return null
        return line.contentScanner().run {
            val hashes = takeRun(HEADING_MARKER)
            take(DECORATIVE_MARKER)
            hashes.takeIf { it in 1..MAX_HEADING_DEPTH && (isAtEnd || peek()?.let(CharCategories::isAsciiWhitespace) == true) }
        }
    }
}

/**
 * The longest suffix that is a whitespace run followed by a `#` run belongs to the syntax, not to the
 * heading's text. Either run may be empty, so a heading ending in `#` loses it.
 * @param from index the content starts at, which the result may not fall below
 * @return index just past the heading's content
 */
private fun Line.endWithoutTrailingHashes(from: Int): Int {
    var index = end
    while (index > from && charAt(index - 1) == HEADING_MARKER) index--
    while (index > from && charAt(index - 1)?.let(CharCategories::isAsciiWhitespace) == true) index--
    return index
}
