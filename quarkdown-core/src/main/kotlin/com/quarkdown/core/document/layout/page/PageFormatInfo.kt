package com.quarkdown.core.document.layout.page

import com.quarkdown.core.ast.quarkdown.block.Container.Alignment
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes

/**
 * Mutable information about the format of all pages of a document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 * @param pageWidth width of each page
 * @param pageHeight height of each page
 * @param margin blank space around the content of each page
 * @param columnCount number of columns on each page. If set, the layout becomes multi-column
 * @param alignment horizontal alignment of the content of each page
 */
data class PageFormatInfo(
    var pageWidth: Size? = null,
    var pageHeight: Size? = null,
    var margin: Sizes? = null,
    var columnCount: Int? = null,
    var alignment: Alignment? = null,
) {
    /**
     * Whether the document has a fixed size.
     */
    val hasSize: Boolean
        get() = pageWidth != null && pageHeight != null
}
