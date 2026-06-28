package com.quarkdown.test.primitive

import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getMediaResources
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for `.extend` applied to Markdown images.
 */
class ImagePrimitiveFunctionTest {
    @Test
    fun `no extension renders unchanged`() {
        execute("![Alt text](image.jpg)") {
            assertEquals("<figure><img src=\"image.jpg\" alt=\"Alt text\" /></figure>", it)
        }
    }

    @Test
    fun `extension wraps every image`() {
        execute(
            """
            .extend {image}
                .container
                    .super

            ![Alt text](image.jpg)
            """.trimIndent(),
        ) {
            assertEquals(
                "<figure><div class=\"container\"><img src=\"image.jpg\" alt=\"Alt text\" /></div></figure>",
                it,
            )
        }
    }

    @Test
    fun `extension wraps every image via function call syntax`() {
        execute(
            """
            .extend {image}
                .container
                    .super

            .image {image.jpg} label:{Alt text}
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\"><figure><img src=\"image.jpg\" alt=\"Alt text\" /></figure></div>",
                it,
            )
        }
    }

    @Test
    fun `extension can override url`() {
        execute(
            """
            .extend {image}
                url:
                .super url:{{.url}\.new}

            ![Alt text](original.jpg)
            """.trimIndent(),
        ) {
            assertEquals(
                "<figure><img src=\"original.jpg.new\" alt=\"Alt text\" /></figure>",
                it,
            )
        }
    }

    @Test
    fun `url override is reflected in media storage`() {
        execute(
            """
            .extend {image}
                .super url:{img/icon.png}

            ![Alt text](nonexistent.png)
            """.trimIndent(),
            enableMediaStorage = true,
            outputResourceHook = { group ->
                val resource = getMediaResources(group).single()
                assertTrue(resource.name.startsWith("icon"))
            },
        ) {
            assertEquals(1, mediaStorage.all.size)
            assertTrue("media/icon" in it.toString(), "Rendered URL should be the media path for the overridden file")
            assertTrue("nonexistent" !in it.toString(), "Original URL must not leak into the output")
        }
    }

    @Test
    fun `extension reaches images nested inside a blockquote`() {
        execute(
            """
            .extend {image}
                .container
                    .super

            > ![Alt text](image.jpg)
            """.trimIndent(),
        ) {
            assertEquals(
                "<blockquote><figure><div class=\"container\"><img src=\"image.jpg\" alt=\"Alt text\" /></div></figure></blockquote>",
                it,
            )
        }
    }

    @Test
    fun `extension applies to multiple images independently`() {
        execute(
            """
            .extend {image}
                .container
                    .super

            ![First](a.jpg)

            ![Second](b.jpg)
            """.trimIndent(),
        ) {
            assertEquals(
                "<figure><div class=\"container\"><img src=\"a.jpg\" alt=\"First\" /></div></figure>" +
                    "<figure><div class=\"container\"><img src=\"b.jpg\" alt=\"Second\" /></div></figure>",
                it,
            )
        }
    }
}
