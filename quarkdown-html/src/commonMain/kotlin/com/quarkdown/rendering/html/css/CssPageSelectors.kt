package com.quarkdown.rendering.html.css

import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.layout.page.PageFormatSelector
import com.quarkdown.core.function.value.data.Range

/**
 * Strategy that converts a [PageFormatSelector] into CSS selectors matching the targeted pages.
 *
 * Each document type lays out its pages differently, hence the elements that represent a page in CSS differ:
 * printed pages are addressed through `@page` at-rules ([PrintCssPageSelectors]),
 * while slides are regular DOM elements ([SlidesCssPageSelectors]).
 */
sealed interface CssPageSelectors {
    /**
     * Whether the produced selectors address CSS page boxes (`@page` at-rules),
     * which accept print-only properties such as `size` and `margin`.
     */
    val isPageBox: Boolean

    /**
     * Converts a selector into the CSS selectors matching the pages it targets.
     * @param selector the scope of the page format, or `null` for all pages
     * @throws IllegalArgumentException if the selector's range has no end bound
     */
    fun toCss(selector: PageFormatSelector?): List<String>

    companion object {
        /**
         * @return the selector strategy matching how [type] documents lay out their pages
         */
        fun forDocumentType(type: DocumentType): CssPageSelectors =
            when (type) {
                DocumentType.SLIDES -> SlidesCssPageSelectors
                else -> PrintCssPageSelectors
            }
    }
}

/**
 * Converts a 1-based page [Range] into a closed [IntRange], defaulting an open start to the first page.
 * @throws IllegalArgumentException if the range has no end bound
 */
internal fun Range.toClosedPageRange(): IntRange {
    val end = requireNotNull(end) { "Open-ended page ranges are not supported in CSS selectors." }
    return (start ?: 1)..end
}
