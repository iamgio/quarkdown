package com.quarkdown.core

import com.quarkdown.core.document.layout.DocumentLayoutInfo
import com.quarkdown.core.document.layout.page.PageFormatInfo
import com.quarkdown.core.document.layout.page.PageSide
import com.quarkdown.core.document.size.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val WIDTH_A4 = Size(210.0, Size.Unit.MILLIMETERS)
private val HEIGHT_A4 = Size(297.0, Size.Unit.MILLIMETERS)
private val HEIGHT_SMALL = Size(6.0, Size.Unit.CENTIMETERS)

private val DEFAULT_FORMAT =
    PageFormatInfo(
        pageWidth = WIDTH_A4,
        pageHeight = HEIGHT_A4,
    )

class DocumentLayoutInfoTest {
    @Test
    fun `no formats and no default returns empty`() {
        val layout = DocumentLayoutInfo()
        assertTrue(layout.getPageFormatsWithDefault(null).isEmpty())
    }

    @Test
    fun `no formats with default returns default`() {
        val layout = DocumentLayoutInfo()
        val result = layout.getPageFormatsWithDefault(DEFAULT_FORMAT)

        assertEquals(1, result.size)
        assertEquals(WIDTH_A4, result.single().pageWidth)
        assertEquals(HEIGHT_A4, result.single().pageHeight)
    }

    @Test
    fun `user format without default is returned as is`() {
        val userFormat = PageFormatInfo(pageHeight = HEIGHT_SMALL)
        val layout = DocumentLayoutInfo(pageFormats = listOf(userFormat))
        val result = layout.getPageFormatsWithDefault(null)

        assertEquals(1, result.size)
        assertNull(result.single().pageWidth)
        assertEquals(HEIGHT_SMALL, result.single().pageHeight)
    }

    @Test
    fun `user format merges with default, user fields take priority`() {
        val userFormat = PageFormatInfo(pageHeight = HEIGHT_SMALL)
        val layout = DocumentLayoutInfo(pageFormats = listOf(userFormat))
        val result = layout.getPageFormatsWithDefault(DEFAULT_FORMAT)

        assertEquals(1, result.size)
        // Width inherited from default, height overridden by user.
        assertEquals(WIDTH_A4, result.single().pageWidth)
        assertEquals(HEIGHT_SMALL, result.single().pageHeight)
    }

    @Test
    fun `multiple user formats with same side are merged`() {
        val first = PageFormatInfo(pageWidth = WIDTH_A4)
        val second = PageFormatInfo(pageHeight = HEIGHT_SMALL, columnCount = 2)
        val layout = DocumentLayoutInfo(pageFormats = listOf(first, second))
        val result = layout.getPageFormatsWithDefault(null)

        assertEquals(1, result.size)
        assertEquals(WIDTH_A4, result.single().pageWidth)
        assertEquals(HEIGHT_SMALL, result.single().pageHeight)
        assertEquals(2, result.single().columnCount)
    }

    @Test
    fun `side-specific formats are separate from global`() {
        val global = PageFormatInfo(pageWidth = WIDTH_A4, pageHeight = HEIGHT_A4)
        val left = PageFormatInfo(side = PageSide.LEFT, pageWidth = WIDTH_A4)
        val layout = DocumentLayoutInfo(pageFormats = listOf(global, left))
        val result = layout.getPageFormatsWithDefault(null)

        assertEquals(2, result.size)

        val globalResult = result.first { it.side == null }
        assertEquals(WIDTH_A4, globalResult.pageWidth)
        assertEquals(HEIGHT_A4, globalResult.pageHeight)

        val leftResult = result.first { it.side == PageSide.LEFT }
        assertEquals(WIDTH_A4, leftResult.pageWidth)
        assertNull(leftResult.pageHeight)
    }

    @Test
    fun `side-specific formats with same side are merged`() {
        val leftMargin = PageFormatInfo(side = PageSide.LEFT, pageWidth = WIDTH_A4)
        val leftColumns = PageFormatInfo(side = PageSide.LEFT, columnCount = 3)
        val layout = DocumentLayoutInfo(pageFormats = listOf(leftMargin, leftColumns))
        val result = layout.getPageFormatsWithDefault(null)

        assertEquals(1, result.size)
        assertEquals(PageSide.LEFT, result.single().side)
        assertEquals(WIDTH_A4, result.single().pageWidth)
        assertEquals(3, result.single().columnCount)
    }

    @Test
    fun `default, global, and side-specific formats coexist`() {
        val userGlobal = PageFormatInfo(pageHeight = HEIGHT_SMALL)
        val left = PageFormatInfo(side = PageSide.LEFT, columnCount = 2)
        val right = PageFormatInfo(side = PageSide.RIGHT, columnCount = 3)
        val layout = DocumentLayoutInfo(pageFormats = listOf(userGlobal, left, right))
        val result = layout.getPageFormatsWithDefault(DEFAULT_FORMAT)

        assertEquals(3, result.size)

        val globalResult = result.first { it.side == null }
        assertEquals(WIDTH_A4, globalResult.pageWidth)
        assertEquals(HEIGHT_SMALL, globalResult.pageHeight)

        val leftResult = result.first { it.side == PageSide.LEFT }
        assertEquals(2, leftResult.columnCount)

        val rightResult = result.first { it.side == PageSide.RIGHT }
        assertEquals(3, rightResult.columnCount)
    }

    @Test
    fun `later user format overrides earlier for same field`() {
        val first = PageFormatInfo(pageHeight = HEIGHT_A4, columnCount = 2)
        val second = PageFormatInfo(pageHeight = HEIGHT_SMALL)
        val layout = DocumentLayoutInfo(pageFormats = listOf(first, second))
        val result = layout.getPageFormatsWithDefault(null)

        assertEquals(1, result.size)
        // Second layer's height wins, first layer's column count is inherited.
        assertEquals(HEIGHT_SMALL, result.single().pageHeight)
        assertEquals(2, result.single().columnCount)
    }
}
