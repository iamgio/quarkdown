package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.block.BlockMatch
import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.LineRules
import com.quarkdown.core.lexer.scan.block.MIN_RESUMPTION_SPACES
import com.quarkdown.core.lexer.scan.block.OrderedBulletPolicy
import com.quarkdown.core.lexer.scan.block.match
import com.quarkdown.core.lexer.scan.block.resumesAfterBlankLines
import com.quarkdown.core.lexer.tokens.ListItemToken

private const val TASK_MARKS = " xX"

private const val TASK_BEGIN = '['

private const val TASK_END = ']'

/**
 * Recognizes one item of a list.
 * Like the list itself, it keeps an explicit loop, because its stopping rule chooses between ending before
 * the terminator, ending after it, and continuing.
 */
object ListItemRecognizer : BlockRecognizer {
    override fun open(cursor: LineCursor): BlockMatch? {
        val line = cursor.current
        val bulletLength = LineRules.bulletLengthOf(line, OrderedBulletPolicy.UP_TO_NINE_DIGITS) ?: return null
        val marker = line.sourceSlice(line.start, line.contentStart + bulletLength)
        val task = line.taskMarkerAt(line.contentStart + bulletLength)
        val afterTask = line.contentStart + bulletLength + task.length
        val separator = line.charAt(afterTask)
        if (separator != ' ' && separator != '\t') return null
        val contentStart = afterTask + 1
        val indentSpaces = line.leadingSpaces
        val wrap: (TokenData) -> Token = { ListItemToken(it, marker, task) }
        if (contentStart == line.end && !cursor.resumes(1, indentSpaces)) {
            return cursor.match(lineCount = 1, endIndex = contentStart, wrap = wrap)
        }
        var offset = 0
        while (offset < cursor.remaining) {
            val current = cursor.peek(offset)!!
            val blocked = cursor.isBlocked(offset)
            val resumes = cursor.resumes(offset + 1, indentSpaces)
            if (blocked && !resumes) {
                return cursor.match(offset + 1, endIndex = current.end, wrap = wrap)
            }
            if (!blocked && !resumes && current.isTerminated && cursor.peek(offset + 1)?.isEmpty == true) {
                return cursor.match(offset + 1, endIndex = current.nextStart, wrap = wrap)
            }
            offset++
        }
        val lastLine = cursor.peek(cursor.remaining - 1)!!
        return cursor.match(cursor.remaining, endIndex = lastLine.nextStart, wrap = wrap)
    }
}

/**
 * Consumes a GFM task marker: a separator, a bracket, one of a space, `x` or `X`, and a closing bracket.
 * @param from index just past the bullet
 * @return the raw task marker including its leading separator, or an empty string when there is none
 */
private fun Line.taskMarkerAt(from: Int): String {
    val separator = charAt(from)
    if (separator != ' ' && separator != '\t') return ""
    val mark = charAt(from + 2)
    if (charAt(from + 1) != TASK_BEGIN || mark == null || mark !in TASK_MARKS || charAt(from + 3) != TASK_END) return ""
    return sourceSlice(from, from + 4)
}

/**
 * An item is blocked when the next line begins a whitespace run of at least two characters that ends in a
 * newline, or begins a bullet.
 * @param offset lines past the cursor whose terminator is being considered
 * @return whether the item is blocked there
 */
private fun LineCursor.isBlocked(offset: Int): Boolean {
    if (peek(offset)?.isTerminated != true) return false
    val first = peek(offset + 1) ?: return false
    if (first.isBlank && (!first.isEmpty || peek(offset + 2)?.isBlank == true)) return true
    return LineRules.bulletLengthOf(first, OrderedBulletPolicy.UP_TO_NINE_DIGITS) != null
}

/**
 * An item resumes when, after any run of blank lines, the next line is indented by the item's own indentation
 * plus at least two more literal spaces. That relative measure keeps an item indented by two spaces from
 * swallowing a sibling indented by three.
 * @param offset lines past the cursor to start looking at
 * @param indentSpaces literal spaces the item itself is indented by
 * @return whether the item resumes
 */
private fun LineCursor.resumes(
    offset: Int,
    indentSpaces: Int,
): Boolean = resumesAfterBlankLines(offset) { it.leadingSpaces >= indentSpaces + MIN_RESUMPTION_SPACES }
