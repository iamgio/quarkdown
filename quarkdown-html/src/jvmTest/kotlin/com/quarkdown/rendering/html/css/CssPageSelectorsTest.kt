package com.quarkdown.rendering.html.css

import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.layout.page.PageFormatSelector
import com.quarkdown.core.document.layout.page.PageSide
import com.quarkdown.core.function.value.data.Range
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [CssPageSelectors], which converts a [PageFormatSelector] into CSS selectors
 * depending on how the target document type lays out its pages.
 */
class CssPageSelectorsTest {
    private val printPage = PrintCssPageSelectors
    private val slides = SlidesCssPageSelectors

    // Strategy lookup

    @Test
    fun `slides document type uses slide selectors`() {
        assertIs<SlidesCssPageSelectors>(CssPageSelectors.forDocumentType(DocumentType.SLIDES))
    }

    @Test
    fun `other document types use print page selectors`() {
        listOf(DocumentType.PLAIN, DocumentType.PAGED, DocumentType.DOCS).forEach {
            assertIs<PrintCssPageSelectors>(CssPageSelectors.forDocumentType(it))
        }
    }

    @Test
    fun `only print page selectors target page boxes`() {
        assertTrue(printPage.isPageBox)
        assertFalse(slides.isPageBox)
    }

    // Print page (@page) selectors

    @Test
    fun `print page global selector`() {
        assertEquals(listOf("@page"), printPage.toCss(null))
    }

    @Test
    fun `print page side selector`() {
        assertEquals(listOf("@page:left"), printPage.toCss(PageFormatSelector(side = PageSide.LEFT)))
    }

    @Test
    fun `print page range expands into one selector per page`() {
        assertEquals(
            listOf("@page:nth(2)", "@page:nth(3)"),
            printPage.toCss(PageFormatSelector(range = Range(2, 3))),
        )
    }

    @Test
    fun `print page open start range starts from first page`() {
        assertEquals(
            listOf("@page:nth(1)", "@page:nth(2)"),
            printPage.toCss(PageFormatSelector(range = Range(null, 2))),
        )
    }

    @Test
    fun `print page side and range combine`() {
        assertEquals(
            listOf("@page:nth(1):right", "@page:nth(2):right"),
            printPage.toCss(PageFormatSelector(side = PageSide.RIGHT, range = Range(1, 2))),
        )
    }

    @Test
    fun `print page rejects open end range`() {
        assertFailsWith<IllegalArgumentException> {
            printPage.toCss(PageFormatSelector(range = Range(2, null)))
        }
    }

    // Slides selectors

    @Test
    fun `slides global selector targets all slides and their backgrounds`() {
        assertEquals(
            listOf(
                ".reveal .slides > :is(section, .pdf-page)",
                ".reveal .backgrounds > .slide-background",
                ".reveal .slides > .pdf-page > .slide-background",
            ),
            slides.toCss(null),
        )
    }

    @Test
    fun `slides range uses a single nth-child range selector`() {
        assertEquals(
            listOf(
                ".reveal .slides > :is(section, .pdf-page):nth-child(n+2):nth-child(-n+3)",
                ".reveal .backgrounds > .slide-background:nth-child(n+2):nth-child(-n+3)",
                ".reveal .slides > .pdf-page:nth-child(n+2):nth-child(-n+3) > .slide-background",
            ),
            slides.toCss(PageFormatSelector(range = Range(2, 3))),
        )
    }

    @Test
    fun `slides open start range omits the lower bound`() {
        assertEquals(
            listOf(
                ".reveal .slides > :is(section, .pdf-page):nth-child(-n+2)",
                ".reveal .backgrounds > .slide-background:nth-child(-n+2)",
                ".reveal .slides > .pdf-page:nth-child(-n+2) > .slide-background",
            ),
            slides.toCss(PageFormatSelector(range = Range(null, 2))),
        )
    }

    @Test
    fun `slides right side maps to odd slides`() {
        assertEquals(
            listOf(
                ".reveal .slides > :is(section, .pdf-page):nth-child(odd)",
                ".reveal .backgrounds > .slide-background:nth-child(odd)",
                ".reveal .slides > .pdf-page:nth-child(odd) > .slide-background",
            ),
            slides.toCss(PageFormatSelector(side = PageSide.RIGHT)),
        )
    }

    @Test
    fun `slides left side maps to even slides`() {
        assertEquals(
            listOf(
                ".reveal .slides > :is(section, .pdf-page):nth-child(even)",
                ".reveal .backgrounds > .slide-background:nth-child(even)",
                ".reveal .slides > .pdf-page:nth-child(even) > .slide-background",
            ),
            slides.toCss(PageFormatSelector(side = PageSide.LEFT)),
        )
    }

    @Test
    fun `slides side and range combine`() {
        assertEquals(
            listOf(
                ".reveal .slides > :is(section, .pdf-page):nth-child(n+2):nth-child(-n+5):nth-child(even)",
                ".reveal .backgrounds > .slide-background:nth-child(n+2):nth-child(-n+5):nth-child(even)",
                ".reveal .slides > .pdf-page:nth-child(n+2):nth-child(-n+5):nth-child(even) > .slide-background",
            ),
            slides.toCss(PageFormatSelector(side = PageSide.LEFT, range = Range(2, 5))),
        )
    }

    @Test
    fun `slides reject open end range`() {
        assertFailsWith<IllegalArgumentException> {
            slides.toCss(PageFormatSelector(range = Range(2, null)))
        }
    }
}
