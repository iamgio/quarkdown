package com.quarkdown.lsp.util

import com.quarkdown.core.util.substringWithinBounds
import com.quarkdown.lsp.model.CursorPosition

/**
 * @param line the line number (0-based)
 * @return the line at the specified index, or null if the index is out of bounds
 */
fun String.getLine(line: Int): String? = lines().getOrNull(line)

/**
 * @param text the text content to search in
 * @return the character at the specified position, or null if the position is out of bounds
 */
fun CursorPosition.getChar(text: String): Char? = text.getLine(line)?.getOrNull(column - 1)

/**
 * @param text the text content to search in
 * @return the substring from the start of the line, up to the specified position, or `null` if the position is out of bounds
 */
fun CursorPosition.getLineUntilPosition(text: String): String? = text.getLine(line)?.substringWithinBounds(0, column)

/**
 * @param text the text content to search in
 * @return the substring that matches the given pattern and contains the given position, or null if no match is found
 */
fun CursorPosition.getByPatternContaining(
    pattern: Regex,
    text: String,
): String? {
    val lineText = text.lines().getOrNull(line) ?: return null
    return pattern
        .findAll(lineText)
        .firstOrNull { it.range.contains(column) }
        ?.value
}

/**
 * Converts a character offset in the text to a [CursorPosition].
 * @param text the text content to search in
 * @param offset the character offset to convert
 * @return the [CursorPosition] corresponding to the given offset
 */
fun offsetToPosition(
    text: String,
    offset: Int,
): CursorPosition {
    var line = 0
    var lastLineStart = 0

    for (i in 0 until offset) {
        if (text[i] == '\n') {
            line++
            lastLineStart = i + 1
        }
    }

    val column = offset - lastLineStart
    return CursorPosition(line, column)
}

/**
 * Converts a [CursorPosition] to a character offset (index) in the text.
 * @param text the text content to search in
 * @return the character offset corresponding to the given position, or -1 if the position is out of bounds
 */
fun CursorPosition.toOffset(text: String): Int {
    val lines = text.lines()
    if (line < 0 || line >= lines.size) return -1
    val lineText = lines[line]
    if (column < 0 || column > lineText.length) return -1
    return lines.take(line).sumOf { it.length + 1 } + column // +1 for the newline character
}
