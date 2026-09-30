package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.CustomIds
import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.block.BlockEnd
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.LineRules
import com.quarkdown.core.lexer.scan.block.OrderedBulletPolicy
import com.quarkdown.core.lexer.scan.block.extent
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.contentScanner
import com.quarkdown.core.lexer.tokens.SetextHeadingToken

private const val UNDERLINE_CHARS = "=-"

/**
 * Recognizes a heading underlined by `=` or `-`.
 * Every line from the first up to the underline is the heading's content; none of them may start with a
 * bullet or be an underline itself.
 */
object SetextHeadingRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        if (!cursor.current.canBeSetextContent()) return null
        var textLines = 1
        while (true) {
            val candidate = cursor.peek(textLines) ?: return null
            if (candidate.underlineChar() != null) break
            if (!candidate.canBeSetextContent()) return null
            textLines++
        }
        val underline = cursor.peek(textLines)!!
        val lastTextLine = cursor.peek(textLines - 1)!!
        val customId = CustomIds.trailing(lastTextLine, lastTextLine.start, lastTextLine.end)
        val content = cursor.current.sourceSlice(cursor.current.start, customId.contentEnd)
        return cursor.match(cursor.extent(textLines + 1, BlockEnd.AFTER_EMPTY_LINES)) { data ->
            SetextHeadingToken(
                data,
                content = content,
                underline = underline.content.trimEnd(),
                customId = customId.value,
            )
        }
    }
}

/**
 * @return whether the line may be part of a setext heading's content
 */
private fun Line.canBeSetextContent(): Boolean =
    !isEmpty &&
        LineRules.bulletLengthOf(this, OrderedBulletPolicy.UP_TO_NINE_DIGITS) == null &&
        underlineChar() == null

/**
 * An underline is at most three columns of indentation, a run of `=` or `-`, then only spaces up to the
 * line's end.
 * @return the underline character, or `null` when the line is not an underline
 */
private fun Line.underlineChar(): Char? {
    if (indent > MAX_BLOCK_INDENT || isBlank) return null
    val marker = charAt(contentStart)?.takeIf { it in UNDERLINE_CHARS } ?: return null
    return contentScanner().run {
        takeRun(marker)
        takeWhile { it == ' ' }
        marker.takeIf { isAtEnd }
    }
}
