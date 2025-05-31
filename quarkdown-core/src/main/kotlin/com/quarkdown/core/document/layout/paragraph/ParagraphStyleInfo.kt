package com.quarkdown.core.document.layout.paragraph

import com.quarkdown.core.document.size.Size

/**
 * Mutable information about the style of paragraphs in a document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 * @param spacing whitespace between paragraphs
 * @param indent whitespace at the start of each paragraph (following LaTeX's convention)
 */
data class ParagraphStyleInfo(
    var spacing: Size? = null,
    var indent: Size? = null,
)
