package com.quarkdown.core.document.layout.page

import com.quarkdown.amber.annotations.Mergeable
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.function.value.data.Range
import com.quarkdown.core.misc.color.Color

/**
 * Immutable information about the format of all pages of a document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 *
 * A document may have multiple [PageFormatInfo] instances with different [selector] values,
 * allowing distinct formatting for specific page sides or page ranges
 * (e.g. mirrored margins in book-style layouts, or different margins for the first few pages).
 *
 * @param selector the scope this format applies to (side, range, or both), or `null` for global (all pages)
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
    val selector: PageFormatSelector? = null,
    val pageWidth: Size? = null,
    val pageHeight: Size? = null,
    val margin: Sizes? = null,
    val contentBorderWidth: Sizes? = null,
    val contentBorderColor: Color? = null,
    val columnCount: Int? = null,
    val alignment: Container.TextAlignment? = null,
)

/**
 * Determines which pages a [PageFormatInfo] applies to.
 * A selector can target a specific [side] (left/right pages), a [range] of 1-based page indices,
 * or both (e.g. left pages within a given range).
 *
 * A `null` selector on [PageFormatInfo] means global scope (all pages).
 *
 * @param side the page side to target, or `null` for both sides
 * @param range 1-based inclusive range of page indices to target, or `null` for all pages
 */
data class PageFormatSelector(
    val side: PageSide? = null,
    val range: Range? = null,
) {
    /**
     * Whether this selector matches all pages (i.e. both [side] and [range] are `null`).
     */
    val isGlobal: Boolean
        get() = side == null && range == null
}

/**
 * Merges a list of [PageFormatInfo] layers into a single instance.
 * Later entries take priority: their non-null fields override earlier ones.
 */
fun List<PageFormatInfo>.mergeAll(): PageFormatInfo = reduce { acc, format -> format.merge(acc) }
