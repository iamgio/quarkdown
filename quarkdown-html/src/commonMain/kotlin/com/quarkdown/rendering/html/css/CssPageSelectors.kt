package com.quarkdown.rendering.html.css

import com.quarkdown.core.document.layout.page.PageFormatSelector

/**
 * Converter of a [PageFormatSelector] into `@page` CSS selectors.
 */
object CssPageSelectors {
    /**
     * Expands a [PageFormatSelector] into individual `@page` CSS selectors.
     * Since `@page` pseudo-class selectors cannot be comma-separated,
     * each page in a range produces its own selector string.
     *
     * - `null`: `["@page"]`
     * - Side-only: `["@page:left"]`
     * - Range-only: `["@page:nth(1)", "@page:nth(2)", ...]`
     * - Side + range: `["@page:nth(1):left", "@page:nth(2):left", ...]`
     *
     * @throws IllegalArgumentException if the range has no end bound
     */
    fun toCss(selector: PageFormatSelector?): List<String> {
        val sideSuffix = selector?.side?.let { ":${it.asCSS}" }.orEmpty()
        val range = selector?.range ?: return listOf("@page$sideSuffix")

        val end = requireNotNull(range.end) { "Open-ended page ranges are not supported in CSS selectors." }
        val start = range.start ?: 1

        return (start..end).map { n -> "@page:nth($n)$sideSuffix" }
    }
}
