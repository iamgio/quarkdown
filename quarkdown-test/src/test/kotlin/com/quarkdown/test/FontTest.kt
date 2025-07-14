package com.quarkdown.test

import com.quarkdown.core.misc.font.FontFamily
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for resolving, loading and applying fonts.
 */
class FontTest {
    private val localPath = "font/NotoSans-Regular.ttf"

    @Test
    fun `local font`() {
        execute(".font main:{$localPath}") {
            val fontConfiguration = documentInfo.layout.font
            val fontFamily = fontConfiguration.mainFamily
            val media = mediaStorage.resolve(localPath)
            assertIs<FontFamily.Media>(fontFamily)
            assertNull(fontConfiguration.headingFamily)
            assertNull(fontConfiguration.codeFamily)
            assertNotNull(media)
            assertEquals(fontFamily.media, media.media)
        }
    }

    @Test
    fun `local font and google font`() {
        execute(".font main:{$localPath} heading:{GoogleFonts:Roboto} code:{GoogleFonts:Source Code Pro}") {
            val fontConfiguration = documentInfo.layout.font
            val media = mediaStorage.resolve(localPath)
            assertIs<FontFamily.Media>(fontConfiguration.mainFamily)
            assertIs<FontFamily.GoogleFont>(fontConfiguration.headingFamily)
            assertIs<FontFamily.GoogleFont>(fontConfiguration.codeFamily)
            assertNotNull(media)
        }
    }
}
