package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.Interruption
import com.quarkdown.core.lexer.scan.block.LineRules
import com.quarkdown.core.lexer.scan.block.MIN_RESUMPTION_SPACES
import com.quarkdown.core.lexer.scan.block.OrderedBulletPolicy
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.block.resumesAfterBlankLines
import com.quarkdown.core.lexer.tokens.OrderedListToken
import com.quarkdown.core.lexer.tokens.UnorderedListToken

/**
 * The two kinds of list, which differ in the bullets that may open and continue them.
 */
enum class ListKind {
    /**
     * A list opened by `*`, `+` or `-` and continued by that same character.
     */
    UNORDERED,

    /**
     * A list opened by up to nine digits and `.` or `)`, and continued by any digits and that same
     * delimiter.
     */
    ORDERED,
}

/**
 * The bullet that opened a list, which a continuation must repeat.
 * @param length amount of characters the bullet spans
 * @param continuationChar character a continuing bullet must end with: the bullet itself when unordered,
 *                         the delimiter when ordered
 */
private class OpeningBullet(
    val length: Int,
    val continuationChar: Char,
)

/**
 * Recognizes a list, ordered or not.
 *
 * It walks the lines in an explicit loop, because standing at the end of a line its stopping rule decides
 * between ending *before* that line's terminator, ending *after* it, and continuing, a three-way outcome no
 * predicate can express.
 *
 * @param interruption what ends the list, supplied by the flavor
 * @param kind which bullets open and continue the list
 */
class ListRecognizer(
    private val interruption: Interruption,
    private val kind: ListKind,
) : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        val line = cursor.current
        val bullet = line.openingBullet(kind) ?: return null
        val afterBullet = line.contentStart + bullet.length
        val separator = line.charAt(afterBullet)
        if (separator != ' ' && separator != '\t') return null
        val contentStart = afterBullet + 1
        val wrap = wrapper(line, bullet)
        if (contentStart == line.end && !cursor.resumes(1, bullet)) {
            return cursor.match(lineCount = 1, endIndex = contentStart, wrap = wrap)
        }
        var offset = 0
        while (offset < cursor.remaining) {
            val current = cursor.peek(offset)!!
            val next = cursor.at(offset + 1)
            val interrupts = next != null && interruption.interrupts(next)
            val resumes = cursor.resumes(offset + 1, bullet)
            if (interrupts && !resumes) {
                return cursor.match(offset + 1, endIndex = current.end, wrap = wrap)
            }
            if (!interrupts && current.isTerminated && cursor.endsAfterNewline(offset, bullet)) {
                return cursor.match(offset + 1, endIndex = current.nextStart, wrap = wrap)
            }
            offset++
        }
        val lastLine = cursor.peek(cursor.remaining - 1)!!
        return cursor.match(cursor.remaining, endIndex = lastLine.nextStart, wrap = wrap)
    }

    private fun wrapper(
        line: Line,
        bullet: OpeningBullet,
    ): (TokenData) -> Token =
        when (kind) {
            ListKind.UNORDERED -> ::UnorderedListToken
            ListKind.ORDERED -> {
                { data ->
                    OrderedListToken(data, marker = line.sourceSlice(line.start, line.contentStart + bullet.length))
                }
            }
        }
}

/**
 * @param kind which bullets may open the list
 * @return the opening bullet, or `null` when the line opens no list of that kind
 */
private fun Line.openingBullet(kind: ListKind): OpeningBullet? {
    val length = LineRules.bulletLengthOf(this, OrderedBulletPolicy.UP_TO_NINE_DIGITS) ?: return null
    val first = charAt(contentStart)!!
    return when (kind) {
        ListKind.UNORDERED -> OpeningBullet(length, first).takeUnless { first.isDigit() }
        ListKind.ORDERED -> OpeningBullet(length, charAt(contentStart + length - 1)!!).takeIf { first.isDigit() }
    }
}

/**
 * A list resumes when, after any run of blank lines, the next line is indented by at least two literal spaces
 * or starts a bullet of the same kind. The spaces are counted literally, not in columns, so a tab-indented
 * line does not resume a list.
 * @param offset lines past the cursor to start looking at
 * @param bullet the bullet that opened the list
 * @return whether the list resumes
 */
private fun LineCursor.resumes(
    offset: Int,
    bullet: OpeningBullet,
): Boolean =
    resumesAfterBlankLines(offset) { line ->
        line.leadingSpaces >= MIN_RESUMPTION_SPACES || line.continuesBullet(bullet)
    }

/**
 * @param bullet the bullet that opened the list
 * @return whether the line starts a bullet of the same kind
 */
private fun Line.continuesBullet(bullet: OpeningBullet): Boolean {
    val length = LineRules.bulletLengthOf(this, OrderedBulletPolicy.UP_TO_NINE_DIGITS) ?: return false
    if (charAt(contentStart + length - 1) != bullet.continuationChar) return false
    val separator = charAt(contentStart + length)
    return separator == ' ' || separator == '\t'
}

/**
 * Decides whether the list ends once the newline has been consumed.
 * @param offset lines past the cursor whose terminator was consumed
 * @param bullet the bullet that opened the list
 * @return whether the list ends there
 */
private fun LineCursor.endsAfterNewline(
    offset: Int,
    bullet: OpeningBullet,
): Boolean {
    val first = peek(offset + 1) ?: return false
    val second = peek(offset + 2)
    if (first.isBlank && second?.isBlank == true) return true
    return first.isEmpty && !resumes(offset + 2, bullet)
}
