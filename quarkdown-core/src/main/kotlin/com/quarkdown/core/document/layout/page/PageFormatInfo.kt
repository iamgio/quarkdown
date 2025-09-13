package com.quarkdown.core.document.layout.page

import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.misc.color.Color
import com.quarkdown.core.util.Defaultable

/**
 * Mutable information about the format of all pages of a document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 *
 * This class is marked as [Defaultable], since its default values can be supplied by the document type
 * via [com.quarkdown.core.document.DocumentType.defaultPageFormat].
 *
 * @param pageWidth width of each page
 * @param pageHeight height of each page
 * @param margin blank space around the content of each page
 * @param contentBorderWidth width of the border around the content area of each page
 * @param contentBorderColor color of the border around the content area of each page
 * @param columnCount number of columns on each page. If set, the layout becomes multi-column
 * @param alignment text alignment of the content of each page
 */
data class PageFormatInfo(
    var pageWidth: Size? = null,
    var pageHeight: Size? = null,
    var margin: Sizes? = null,
    var contentBorderWidth: Sizes? = null,
    var contentBorderColor: Color? = null,
    var columnCount: Int? = null,
    var alignment: Container.TextAlignment? = null,
) : Defaultable
