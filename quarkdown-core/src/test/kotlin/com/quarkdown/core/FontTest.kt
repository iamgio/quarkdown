package com.quarkdown.core

import com.quarkdown.core.misc.font.FontFamily
import com.quarkdown.core.misc.font.resolver.FontFamilyResolver
import org.junit.Assume.assumeTrue
import java.awt.GraphicsEnvironment
import java.io.File
import kotlin.test.Test
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
    fun `media font`() {
        assertIs<FontFamily.Media>(
            FontFamilyResolver.SYSTEM.resolve("path/to/font.ttf", workingDirectory = File(".")),
        )
    }
}
