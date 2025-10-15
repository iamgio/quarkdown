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
            val fontConfiguration = documentInfo.layout.fonts.first()
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
            val fontConfiguration = documentInfo.layout.fonts.first()
            val media = mediaStorage.resolve(localPath)
            assertIs<FontFamily.Media>(fontConfiguration.mainFamily)
            assertIs<FontFamily.GoogleFont>(fontConfiguration.headingFamily)
            assertIs<FontFamily.GoogleFont>(fontConfiguration.codeFamily)
            assertNotNull(media)
        }
    }

    @Test
    fun `multiple font configurations`() {
        execute(
            """
            .font main:{GoogleFonts:Roboto}
            .font main:{$localPath} heading:{GoogleFonts:Source Sans Pro}
            .font code:{GoogleFonts:Source Code Pro}
            """.trimIndent(),
        ) {
            val fontConfigurations = documentInfo.layout.fonts
            assertEquals(3, fontConfigurations.size)
            assertIs<FontFamily.GoogleFont>(fontConfigurations[0].mainFamily)
            assertIs<FontFamily.Media>(fontConfigurations[1].mainFamily)
            assertIs<FontFamily.GoogleFont>(fontConfigurations[1].headingFamily)
            assertIs<FontFamily.GoogleFont>(fontConfigurations[2].codeFamily)
        }
    }
}
