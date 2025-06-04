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
