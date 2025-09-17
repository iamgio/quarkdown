package com.quarkdown.core.document.layout.paragraph

import com.quarkdown.automerge.annotations.Mergeable

/**
 * Immutable information about the style of paragraphs in a document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 * @param lineHeight height of each line, multiplied by the font size
 * @param letterSpacing whitespace between letters, multiplied by the font size
 * @param spacing whitespace between paragraphs, multiplied by the font size
 * @param indent whitespace at the start of each paragraph, following LaTeX's policy, multiplied by the font size
 */
@Mergeable
data class ParagraphStyleInfo(
    val lineHeight: Double? = null,
    val letterSpacing: Double? = null,
    val spacing: Double? = null,
    val indent: Double? = null,
)
