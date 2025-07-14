package com.quarkdown.core

import com.quarkdown.core.misc.font.FontFamily
import com.quarkdown.core.misc.font.resolver.FontFamilyResolver
import org.junit.Assume.assumeTrue
import java.awt.GraphicsEnvironment
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for font-related operations.
 */
class FontTest {
    @Test
    fun `system font`() {
        val name = "Impact"
        assumeTrue(name in GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames)
        assertIs<FontFamily.System>(
            FontFamilyResolver.SYSTEM.resolve(name, workingDirectory = File(".")),
        )
    }

    @Test
    fun `local media font`() {
        assertIs<FontFamily.Media>(
            FontFamilyResolver.SYSTEM.resolve("path/to/font.ttf", workingDirectory = File(".")),
        )
    }

    @Test
    fun `remote media font`() {
        val url = "https://example.com/fonts/font.ttf"
        assertIs<FontFamily.Media>(
            FontFamilyResolver.SYSTEM.resolve(url, workingDirectory = null),
        )
    }

    @Test
    fun `google font`() {
        val font = FontFamilyResolver.SYSTEM.resolve("GoogleFonts:Noto Sans", workingDirectory = null)
        assertIs<FontFamily.GoogleFont>(font)
        assertEquals("Noto Sans", font.name)
    }
}
