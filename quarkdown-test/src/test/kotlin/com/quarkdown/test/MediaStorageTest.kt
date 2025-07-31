package com.quarkdown.test

import com.quarkdown.core.media.storage.MEDIA_SUBDIRECTORY_NAME
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getSubResources
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for the media storage system.
 */
class MediaStorageTest {
    private fun getMediaResources(group: OutputResource?): Set<OutputResource> {
        val mediaGroup = getSubResources(group).find { it.name == MEDIA_SUBDIRECTORY_NAME }
        assertIs<OutputResourceGroup>(mediaGroup)
        return mediaGroup.resources
    }

    @Test
    fun `local media, no media storage`() {
        execute(
            """
            This is the Quarkdown logo: ![Quarkdown](img/icon.png).                                 
            """.trimIndent(),
            enableMediaStorage = false,
        ) {
            assertEquals("<p>This is the Quarkdown logo: <img src=\"img/icon.png\" alt=\"Quarkdown\" />.</p>", it)
            assertEquals(0, mediaStorage.all.size)
        }
    }

    @Test
    fun `local media, with media storage`() {
        execute(
            """
            This is the Quarkdown logo: ![Quarkdown](img/icon.png).                                 
            """.trimIndent(),
            enableMediaStorage = true,
            outputResourceHook = { group ->
                assertTrue(getMediaResources(group).single().name.startsWith("icon"))
            },
        ) {
            assertEquals("<p>This is the Quarkdown logo: <img src=\"media/icon", it.toString().substringBefore("@"))
            // The file name is "media/icon-[encoded].png"
            assertEquals("\" alt=\"Quarkdown\" />.</p>", it.toString().substringAfter(".png"))
        }
    }

    @Test
    fun `remote media, with media storage`() {
        execute(
            """
            .container
                ![Icon](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/ticon-light.svg "The Quarkdown icon")
                
                ![Banner](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg)
            """.trimIndent(),
            options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = true),
            enableMediaStorage = true,
            outputResourceHook = { group ->
                assertEquals(
                    "https-raw.githubusercontent.com-iamgio-quarkdown-project-files-images-ticon-light.svg",
                    getMediaResources(group).single { "ticon" in it.name }.name,
                )
            },
        ) {
            assertEquals(
                "<div class=\"container\">" +
                    "<figure>" +
                    "<img src=\"media/https-raw.githubusercontent.com-iamgio-quarkdown-project-files-images-ticon-light.svg\" " +
                    "alt=\"Icon\" title=\"The Quarkdown icon\" />" +
                    "<figcaption class=\"caption-bottom\">The Quarkdown icon</figcaption>" +
                    "</figure>" +
                    "<figure>" +
                    "<img src=\"media/https-raw.githubusercontent.com-iamgio-quarkdown-project-files-images-tbanner-light.svg\" " +
                    "alt=\"Banner\" />" +
                    "</figure>" +
                    "</div>",
                it,
            )

            assertEquals(2, mediaStorage.all.size)
        }
    }

    @Test
    fun `remote and local media, with local media storage only`() {
        execute(
            """
            ![Banner](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg)  
            ![Quarkdown](img/icon.png)
            """.trimIndent(),
            options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = false),
            enableMediaStorage = true,
        ) {
            assertEquals(
                "<p>" +
                    "<img src=\"https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg\" " +
                    "alt=\"Banner\" /><br /><img src=\"media/",
                it.toString().substringBefore("icon@"),
            )

            assertEquals(1, mediaStorage.all.size)
        }
    }

    @Test
    fun `remote and local media, with media storage`() {
        execute(
            """
            [Banner]: https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg
            ![Banner]
            """.trimIndent(),
            options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = true),
            enableMediaStorage = true,
        ) {
            assertEquals(
                "<p><img src=\"media/https-raw.githubusercontent.com-iamgio-quarkdown-project-files-images-tbanner-light.svg\" " +
                    "alt=\"Banner\" /></p>",
                it,
            )
        }
    }

    @Test
    fun `transitive media from subdocument`() {
        execute(
            source = "[1](subdoc/media.qd)",
            enableMediaStorage = true,
            outputResourceHook = { group ->
                assertNotNull(getMediaResources(group).singleOrNull { it.name.startsWith("icon") })
            },
        ) {}
    }
}
