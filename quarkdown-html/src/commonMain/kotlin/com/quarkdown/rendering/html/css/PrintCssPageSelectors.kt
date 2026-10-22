package com.quarkdown.rendering.html.css

import com.quarkdown.core.document.layout.page.PageFormatSelector

/**
 * `@page` selectors for printed pages, processed by the browser's print engine or by Paged.js.
 *
 * Since `@page` pseudo-class selectors cannot be comma-separated,
 * each page in a range produces its own selector string.
 *
 * - `null`: `["@page"]`
 * - Side-only: `["@page:left"]`
 * - Range-only: `["@page:nth(1)", "@page:nth(2)", ...]`
 * - Side + range: `["@page:nth(1):left", "@page:nth(2):left", ...]`
 */
data object PrintCssPageSelectors : CssPageSelectors {
    override val isPageBox = true

    override fun toCss(selector: PageFormatSelector?): List<String> {
        val sideSuffix = selector?.side?.let { ":${it.asCSS}" }.orEmpty()
        val range = selector?.range ?: return listOf("@page$sideSuffix")

        return range.toClosedPageRange().map { n -> "@page:nth($n)$sideSuffix" }
    }
}
