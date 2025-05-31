package com.quarkdown.core.document.layout.paragraph

/**
 * Mutable information about the style of paragraphs in a document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 * @param spacing whitespace between paragraphs, multiplied by the font size
 * @param indent whitespace at the start of each paragraph, following LaTeX's policy, multiplied by the font size
 */
data class ParagraphStyleInfo(
    var spacing: Double? = null,
    var indent: Double? = null,
)
