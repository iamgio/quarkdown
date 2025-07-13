package com.quarkdown.test

import com.quarkdown.core.misc.font.FontFamily
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

/**
 *
 */
class FontTest {
    @Test
    fun `local font`() {
        val path = "font/NotoSans-Regular.ttf"
        execute(".pageformat font:{$path}") {
            val fontFamily = documentInfo.layout.pageFormat.fontFamily
            val media = mediaStorage.resolve(path)
            assertIs<FontFamily.Media>(fontFamily)
            assertNotNull(media)
            assertEquals(fontFamily.media, media.media)
        }
    }
}
