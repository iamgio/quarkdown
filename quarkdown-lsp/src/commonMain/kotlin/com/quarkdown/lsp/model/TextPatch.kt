package com.quarkdown.lsp.model

/**
 * A text replacement within a document.
 * @param start the start position of the replaced region, inclusive
 * @param end the end position of the replaced region, exclusive
 * @param text the replacement text. An empty string deletes the region,
 *        and a patch with `start == end` inserts at that position
 */
data class TextPatch(
    val start: CursorPosition,
    val end: CursorPosition,
    val text: String,
)
