package com.quarkdown.cli

import com.quarkdown.cli.creator.content.DefaultTheme
import com.quarkdown.core.document.DocumentType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the default theme components assigned to new projects by [DefaultTheme].
 */
class DefaultThemeTest {
    @Test
    fun `layout theme per document type`() {
        assertEquals("latex", DefaultTheme.getLayoutTheme(DocumentType.PLAIN))
        assertEquals("latex", DefaultTheme.getLayoutTheme(DocumentType.PAGED))
        assertEquals("focus", DefaultTheme.getLayoutTheme(DocumentType.SLIDES))
        assertEquals("hyperlegible", DefaultTheme.getLayoutTheme(DocumentType.DOCS))
    }

    @Test
    fun `color theme per document type`() {
        assertEquals("paperwhite", DefaultTheme.getColorTheme(DocumentType.PLAIN))
        assertEquals("paperwhite", DefaultTheme.getColorTheme(DocumentType.PAGED))
        assertEquals("paperwhite", DefaultTheme.getColorTheme(DocumentType.SLIDES))
        assertEquals("galactic", DefaultTheme.getColorTheme(DocumentType.DOCS))
    }
}
