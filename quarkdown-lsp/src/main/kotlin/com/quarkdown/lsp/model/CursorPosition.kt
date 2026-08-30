package com.quarkdown.lsp.model

/**
 * A zero-based position within a text document.
 * @param line the line index
 * @param column the character index within the line
 */
data class CursorPosition(
    val line: Int,
    val column: Int,
)
