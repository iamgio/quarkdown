package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.BACKTICK
import com.quarkdown.core.lexer.scan.CustomIds
import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.Matchers
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.extendThrough
import com.quarkdown.core.lexer.scan.block.extendToEnd
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.contentScanner
import com.quarkdown.core.lexer.scan.scanner
import com.quarkdown.core.lexer.scan.titleOpeningFor
import com.quarkdown.core.lexer.tokens.FencesCodeToken

/**
 * Shortest run of fence characters that opens or closes a fenced block.
 */
internal const val MIN_FENCE_LENGTH = 3

private const val TILDE_FENCE = '~'

private val FENCE_CHARS = charArrayOf(BACKTICK, TILDE_FENCE)

/**
 * The parts a fence header declares after its opening run.
 * @param language raw language tag, if any
 * @param caption raw caption including its delimiters, if any
 * @param customId raw cross-reference identifier, if any
 */
private class FenceHeader(
    val language: String?,
    val caption: String?,
    val customId: String?,
)

/**
 * Recognizes content fenced by three or more backticks or tildes.
 * The block ends at the first line that holds nothing but a run of the same character, at least as long as the
 * opening one, and trailing whitespace. A longer opening fence therefore wraps shorter fences as content, and
 * a block whose fence is never closed runs to the end of the document.
 */
object FencedCodeRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        val opener = cursor.current
        if (opener.indent > MAX_BLOCK_INDENT) return null
        val fenceChar = opener.openingFenceChar() ?: return null
        val fenceLength = opener.countContentRun(fenceChar)
        val header = opener.parseFenceHeader(opener.contentStart + fenceLength)
        val (extent, fenceStart) =
            cursor.extendThrough { it.closingFenceStart(fenceChar, fenceLength) }
                ?: (cursor.extendToEnd() to cursor.peek(cursor.remaining - 1)!!.end)
        val content = opener.sourceSlice(opener.end, fenceStart)
        return cursor.match(extent) { data ->
            FencesCodeToken(
                data,
                indent = opener.indent,
                language = header.language,
                caption = header.caption,
                customId = header.customId,
                content = content,
            )
        }
    }

    /**
     * An unterminated last line opens nothing, since a fence needs a line of its own.
     * @param line line to inspect
     * @return whether the line opens a code fence
     */
    fun opensAt(line: Line): Boolean = line.isTerminated && line.openingFenceChar() != null
}

/**
 * Parses the header as `language caption customId`, right to left: the custom ID is the trailing `{#...}`,
 * the caption is the delimited title before it, and the language is whatever is left. So a header may carry
 * a caption, or a custom ID, without a language.
 * @param from index just past the opening fence run
 * @return the parsed header, which always parses because every part is optional
 */
private fun Line.parseFenceHeader(from: Int): FenceHeader {
    val customId = CustomIds.trailing(this, from, end)
    val captionStart = trailingTitleStart(from, customId.contentEnd)
    val caption = captionStart?.let { sourceSlice(it, customId.contentEnd) }
    val languageEnd = captionStart?.let { trimmedEnd(from, it) } ?: customId.contentEnd
    val language =
        scanner(from, languageEnd).run {
            takeSpacesAndTabs()
            takeRest().takeIf { it.isNotEmpty() }
        }
    return FenceHeader(language, caption, customId.value)
}

/**
 * @param from index the result may not fall below
 * @param end index just past the slice whose trailing spaces and tabs are dropped
 * @return the index where that trailing run begins
 */
private fun Line.trimmedEnd(
    from: Int,
    end: Int,
): Int = (end downTo from + 1).firstOrNull { charAt(it - 1) != ' ' && charAt(it - 1) != '\t' } ?: from

/**
 * @param from index the header's content starts at, which the title may not start before
 * @param end index just past the slice the title must end at
 * @return the index a trailing delimited title starts at, or `null` when the slice does not end with one
 */
private fun Line.trailingTitleStart(
    from: Int,
    end: Int,
): Int? {
    val closing = charAt(end - 1) ?: return null
    val opening = titleOpeningFor(closing) ?: return null
    val source = sourceOf()
    return (end - 2 downTo from).firstOrNull { Matchers.unescapedMatch(source, it, opening) != 0 }
}

/**
 * A closing fence takes a line of its own: at most [MAX_BLOCK_INDENT] columns of indentation, a run of the
 * fence character at least as long as the opening one, then nothing but spaces and tabs. A run in the middle
 * of a line is content, so `` a``` `` inside a block does not end it.
 * @param fenceChar the character the opening run was made of
 * @param minLength shortest closing run that counts
 * @return the index the closing run starts at, or `null` when the line is no closing fence
 */
internal fun Line.closingFenceStart(
    fenceChar: Char,
    minLength: Int = MIN_FENCE_LENGTH,
): Int? {
    if (indent > MAX_BLOCK_INDENT) return null
    val scanner = contentScanner()
    val runStart = scanner.mark()
    if (scanner.takeRun(fenceChar) < minLength) return null
    scanner.takeSpacesAndTabs()
    return runStart.takeIf { scanner.isAtEnd }
}

/**
 * @return the character the line's opening fence run is made of, or `null` when the line opens no fence
 */
private fun Line.openingFenceChar(): Char? {
    if (indent > MAX_BLOCK_INDENT) return null
    val candidate = charAt(contentStart)?.takeIf { it in FENCE_CHARS } ?: return null
    return candidate.takeIf { countContentRun(it) >= MIN_FENCE_LENGTH }
}
