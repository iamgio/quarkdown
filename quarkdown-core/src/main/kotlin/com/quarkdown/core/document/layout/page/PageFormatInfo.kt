package com.quarkdown.core.document.layout.page

import com.quarkdown.amber.annotations.Mergeable
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.misc.color.Color

/**
 * Immutable information about the format of all pages of a document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 *
 * A document may have multiple [PageFormatInfo] instances with different [side] values,
 * allowing distinct formatting for left and right pages (e.g. mirrored margins in paged documents).
 *
 * @param side the page side this format applies to, or `null` for both sides
 * @param pageWidth width of each page
 * @param pageHeight height of each page
 * @param margin blank space around the content of each page
 * @param contentBorderWidth width of the border around the content area of each page
 * @param contentBorderColor color of the border around the content area of each page
 * @param columnCount number of columns on each page. If set, the layout becomes multi-column
 * @param alignment text alignment of the content of each page
 */
@Mergeable
data class PageFormatInfo(
    val side: PageSide? = null,
    val pageWidth: Size? = null,
    val pageHeight: Size? = null,
    val margin: Sizes? = null,
    val contentBorderWidth: Sizes? = null,
    val contentBorderColor: Color? = null,
    val columnCount: Int? = null,
    val alignment: Container.TextAlignment? = null,
)

/**
 * Merges a list of [PageFormatInfo] layers into a single instance.
 * Later entries take priority: their non-null fields override earlier ones.
 */
fun List<PageFormatInfo>.mergeAll(): PageFormatInfo = reduce { acc, format -> format.merge(acc) }
