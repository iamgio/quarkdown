package com.quarkdown.core.document.layout.page

import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.misc.color.Color
import com.quarkdown.core.misc.font.FontFamily

/**
 * Mutable information about the format of all pages of a document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 * @param pageWidth width of each page
 * @param pageHeight height of each page
 * @param margin blank space around the content of each page
 * @param fontFamily font family of the text on each page
 * @param fontSize font size of the text on each page
 * @param contentBorderWidth width of the border around the content area of each page
 * @param contentBorderColor color of the border around the content area of each page
 * @param columnCount number of columns on each page. If set, the layout becomes multi-column
 * @param alignment text alignment of the content of each page
 */
data class PageFormatInfo(
    var pageWidth: Size? = null,
    var pageHeight: Size? = null,
    var margin: Sizes? = null,
    var fontFamily: FontFamily? = null,
    var fontSize: Size? = null,
    var contentBorderWidth: Sizes? = null,
    var contentBorderColor: Color? = null,
    var columnCount: Int? = null,
    var alignment: Container.TextAlignment? = null,
) {
    /**
     * Whether the document has a fixed size.
     */
    val hasSize: Boolean
        get() = pageWidth != null && pageHeight != null
}
