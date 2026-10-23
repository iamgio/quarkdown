package com.quarkdown.rendering.html.css

import com.quarkdown.core.document.layout.page.PageFormatSelector
import com.quarkdown.core.document.layout.page.PageSide
import com.quarkdown.core.function.value.data.Range

/**
 * Selectors for Reveal.js slides, where each page is a `section` of the presentation.
 *
 * Two kinds of elements are targeted for each slide:
 * - The slide element itself (`section`, or its `.pdf-page` wrapper in print mode),
 *   where inherited properties such as column count and alignment take effect;
 * - Its background element, where background color and borders are painted.
 *
 * Slides are indexed via `:nth-child`, hence a range maps to a single `:nth-child(n+start):nth-child(-n+end)` selector,
 * and a side maps to odd (right, as the first page is recto) or even (left) slides.
 */
data object SlidesCssPageSelectors : CssPageSelectors {
    override val isPageBox = false

    override fun toCss(selector: PageFormatSelector?): List<String> {
        val pseudo = selector?.let(::pseudoClasses).orEmpty()
        return listOf(
            ".reveal .slides > :is(section, .pdf-page)$pseudo",
            ".reveal .backgrounds > .slide-background$pseudo",
            ".reveal .slides > .pdf-page$pseudo > .slide-background",
        )
    }

    /**
     * Builds the chain of `:nth-child` pseudo-classes that narrows a slide element down to the scope of [selector],
     * combining [rangePseudoClasses] and [sidePseudoClasses] when both a range and a side are set.
     *
     * @param selector the scope to translate
     * @return the pseudo-classes to append to a slide selector, e.g. `:nth-child(n+2):nth-child(-n+5):nth-child(even)`
     * @throws IllegalArgumentException if the selector's range has no end bound
     */
    private fun pseudoClasses(selector: PageFormatSelector): String =
        selector.range?.let(::rangePseudoClasses).orEmpty() +
            selector.side?.let(::sidePseudoClasses).orEmpty()

    /**
     * Translates a 1-based page range into `:nth-child` bounds:
     * `n+start` selects the slide at `start` and all following ones, `-n+end` selects the slide at `end` and all preceding ones,
     * so their intersection covers exactly the range. The lower bound is omitted when it is the first slide, as it would match everything.
     *
     * @param range the range of slide indices
     * @return e.g. `:nth-child(n+2):nth-child(-n+5)` for `2..5`, or `:nth-child(-n+5)` for `..5`
     * @throws IllegalArgumentException if [range] has no end bound
     */
    private fun rangePseudoClasses(range: Range): String =
        buildString {
            val (start, end) = range.toClosedPageRange().let { it.first to it.last }
            if (start > 1) {
                append(":nth-child(n+$start)")
            }
            append(":nth-child(-n+$end)")
        }

    /**
     * Translates a page side into a `:nth-child` parity.
     * As the first page is recto, right pages map to odd slides and left pages to even slides.
     *
     * @param side the page side
     * @return `:nth-child(odd)` or `:nth-child(even)`
     */
    private fun sidePseudoClasses(side: PageSide): String =
        when (side) {
            PageSide.RIGHT -> ":nth-child(odd)"
            PageSide.LEFT -> ":nth-child(even)"
        }
}
