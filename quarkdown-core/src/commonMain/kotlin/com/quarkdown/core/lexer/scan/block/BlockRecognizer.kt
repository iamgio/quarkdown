package com.quarkdown.core.lexer.scan.block

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor

/**
 * A block produced by a [BlockRecognizer].
 * @param token the produced token
 * @param lineCount amount of lines the block consumed, at least one
 * @param scanEnd index the scan resumes its end-of-input check from, which differs from the token's own
 *                end only for the zero-width function call flag, whose extent comes from its walker
 */
class BlockMatch(
    val token: Token,
    val lineCount: Int,
    val scanEnd: Int,
)

/**
 * Where a block's text ends relative to the last line it consumed.
 */
enum class BlockEnd {
    /**
     * Just before the terminator of the last line, which the block lexer then drops, since it fills no gaps.
     */
    BEFORE_TERMINATOR,

    /**
     * Just past the terminator of the last line.
     */
    AFTER_TERMINATOR,

    /**
     * Just past the terminator of the last line and of every empty line that follows it.
     */
    AFTER_EMPTY_LINES,

    /**
     * Just past the terminator of the last line and of every blank line that follows it, whitespace-only
     * lines included.
     */
    AFTER_BLANK_LINES,
}

/**
 * How far a block reaches from the line it opened on, with both halves of the decision resolved together so
 * a recognizer cannot pair a line count with the wrong end index.
 * @param lineCount amount of lines consumed, at least one
 * @param endIndex index just past the last character the token's text covers
 */
class BlockExtent(
    val lineCount: Int,
    val endIndex: Int,
)

/**
 * Resolves an extent of a known amount of lines.
 * @param lineCount amount of lines consumed, before any empty lines [end] absorbs
 * @param end where the block's text ends relative to its last line
 * @return the resolved extent
 */
fun LineCursor.extent(
    lineCount: Int = 1,
    end: BlockEnd = BlockEnd.BEFORE_TERMINATOR,
): BlockExtent {
    val total =
        when (end) {
            BlockEnd.AFTER_EMPTY_LINES -> lineCount + emptyLinesAfter(lineCount)
            BlockEnd.AFTER_BLANK_LINES -> lineCount + blankLinesAfter(lineCount)
            else -> lineCount
        }
    val endIndex =
        when (end) {
            BlockEnd.BEFORE_TERMINATOR -> peek(total - 1)!!.end
            else -> spanTo(total)
        }
    return BlockExtent(total, endIndex)
}

/**
 * Resolves an extent that grows for as long as [continues] accepts the line after it. Paragraphs, block
 * quotes, footnote definitions, table rows and blank runs differ only in the predicate and in [end].
 * @param from amount of lines already consumed, at least one
 * @param end where the block's text ends relative to its last line
 * @param continues tested on a cursor positioned at the candidate next line, which can therefore look both
 *                  forward and back with [LineCursor.peek]
 * @return the resolved extent
 */
fun LineCursor.extendWhile(
    from: Int = 1,
    end: BlockEnd = BlockEnd.BEFORE_TERMINATOR,
    continues: (LineCursor) -> Boolean,
): BlockExtent {
    var lineCount = from
    while (true) {
        val next = at(lineCount) ?: break
        if (!continues(next)) break
        lineCount++
    }
    return extent(lineCount, end)
}

/**
 * Resolves an extent that grows up to and including the first line [closes] accepts, which is the shape of
 * every fenced block.
 * @param from amount of lines already consumed, at least one
 * @param end where the block's text ends relative to its closing line
 * @param closes returns what the closing line yielded, or `null` when the line does not close the block
 * @return the resolved extent and what [closes] yielded, or `null` when the block never closes
 */
fun <T : Any> LineCursor.extendThrough(
    from: Int = 1,
    end: BlockEnd = BlockEnd.BEFORE_TERMINATOR,
    closes: (Line) -> T?,
): Pair<BlockExtent, T>? {
    var lineCount = from
    while (true) {
        val line = peek(lineCount) ?: return null
        val closed = closes(line)
        if (closed != null) return extent(lineCount + 1, end) to closed
        lineCount++
    }
}

/**
 * Covers every line left, which is where a block that never closes ends.
 * @param end where the extent stops relative to the last line's terminator
 * @return the extent of the rest of the input
 */
fun LineCursor.extendToEnd(end: BlockEnd = BlockEnd.BEFORE_TERMINATOR): BlockExtent = extent(remaining, end)

/**
 * @param lineCount amount of lines counted from the cursor
 * @return amount of empty lines right after those lines
 */
fun LineCursor.emptyLinesAfter(lineCount: Int): Int {
    var extra = 0
    while (peek(lineCount + extra)?.isEmpty == true) extra++
    return extra
}

/**
 * @param lineCount amount of lines counted from the cursor
 * @return amount of blank lines right after those lines
 */
fun LineCursor.blankLinesAfter(lineCount: Int): Int {
    var extra = 0
    while (peek(lineCount + extra)?.isBlank == true) extra++
    return extra
}

/**
 * A strategy that attempts to open a block at a line.
 * Recognizers are tried in priority order at every line start, and the first match wins.
 */
fun interface BlockRecognizer {
    /**
     * Whether the recognizer only opens at the start of a line.
     */
    val isLineAnchored: Boolean
        get() = true

    /**
     * @param cursor the line to attempt the match at
     * @return the match, or `null` when this recognizer does not apply here
     */
    fun open(cursor: LineCursor): BlockMatch?
}

/**
 * Builds a match whose token covers the source from the cursor's line up to [endIndex].
 * Prefer the [BlockExtent] overload, which resolves the line count and the end together. This one serves the
 * kinds whose text is not line-aligned: the comment, the page break and the zero-width function call flag.
 * @param lineCount amount of lines the block consumed
 * @param endIndex index just past the last character the token's text covers
 * @param wrap constructor of the token
 * @return the resulting match
 */
fun LineCursor.match(
    lineCount: Int,
    endIndex: Int,
    wrap: (TokenData) -> Token,
): BlockMatch {
    val start = current.start
    val data = TokenData(text = current.sourceSlice(start, endIndex), position = start until endIndex)
    return BlockMatch(wrap(data), lineCount, scanEnd = endIndex)
}

/**
 * Builds a match from a resolved [BlockExtent].
 * @param extent how far the block reaches
 * @param wrap constructor of the token
 * @return the resulting match
 */
fun LineCursor.match(
    extent: BlockExtent,
    wrap: (TokenData) -> Token,
): BlockMatch = match(extent.lineCount, extent.endIndex, wrap)

/**
 * Literal spaces of extra indentation that make a line continue a list, rather than end it.
 */
internal const val MIN_RESUMPTION_SPACES = 2

/**
 * Walks past any run of blank lines and asks [resumes] about the first line that follows: a list and a list
 * item differ only in that question.
 * @param offset lines past the cursor to start looking at
 * @param resumes whether the first non-blank line resumes the block
 * @return whether the block resumes
 */
internal inline fun LineCursor.resumesAfterBlankLines(
    offset: Int,
    resumes: (Line) -> Boolean,
): Boolean {
    var index = offset
    while (true) {
        val line = peek(index) ?: return false
        if (resumes(line)) return true
        if (!line.isBlank) return false
        index++
    }
}
