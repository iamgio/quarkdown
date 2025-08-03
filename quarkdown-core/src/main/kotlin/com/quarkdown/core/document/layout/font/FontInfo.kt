package com.quarkdown.core.document.layout.font

import com.quarkdown.core.document.size.Size
import com.quarkdown.core.misc.font.FontFamily

/**
 * Mutable information about the global font configuration of a document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 * @param mainFamily font family of generic content
 * @param headingFamily font family of headings
 * @param codeFamily font family of code blocks and code spans
 * @param size font size of generic content. Other elements will scale accordingly
 */
data class FontInfo(
    var mainFamily: FontFamily? = null,
    var headingFamily: FontFamily? = null,
    var codeFamily: FontFamily? = null,
    var size: Size? = null,
)
