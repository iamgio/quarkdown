package com.quarkdown.lsp.util

import org.eclipse.lsp4j.Position

/**
 * @param text the text content to search in
 * @return the character at the specified position, or null if the position is out of bounds
 */
fun Position.getChar(text: String): Char? = text.lines().getOrNull(line)?.getOrNull(character - 1)

/**
 * @param text the text content to search in
 * @return the substring that matches the given pattern and contains the given position, or null if no match is found
 */
fun Position.getByPatternContaining(
    pattern: Regex,
    text: String,
): String? {
    val lineText = text.lines().getOrNull(line) ?: return null
    return pattern
        .findAll(lineText)
        .firstOrNull { it.range.contains(character) }
        ?.value
}

/**
 * Converts a character offset in the text to a [Position].
 * @param text the text content to search in
 * @param offset the character offset to convert
 * @return the [Position] corresponding to the given offset
 */
fun offsetToPosition(
    text: String,
    offset: Int,
): Position {
    var line = 0
    var lastLineStart = 0

    for (i in 0 until offset) {
        if (text[i] == '\n') {
            line++
            lastLineStart = i + 1
        }
    }

    val character = offset - lastLineStart
    return Position(line, character)
}
