package com.quarkdown.lsp.util

import org.eclipse.lsp4j.Position

/**
 * @param text the text content to search in
 * @return the character at the specified position, or null if the position is out of bounds
 */
fun Position.getChar(text: String): Char? = text.lines().getOrNull(line)?.getOrNull(character - 1)
