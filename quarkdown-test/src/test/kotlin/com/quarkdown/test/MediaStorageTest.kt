package com.quarkdown.test

import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.media.storage.MEDIA_SUBDIRECTORY_NAME
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.INDEX
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getMediaResources
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for the media storage system.
 */
class MediaStorageTest {
    @Test
    fun `local media, no media storage`() {
        execute(
            """
            This is the Quarkdown logo: ![Quarkdown](img/icon.png).                                 
            """.trimIndent(),
            enableMediaStorage = false,
            outputResourceHook = { group ->
                assertFails { getMediaResources(group) }
            },
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
            outputResourceHook = { group ->
                assertTrue(getMediaResources(group).singleOrNull { it.name.startsWith("icon") } != null)
                assertFails { getMediaResources(group).single { it.name.startsWith("tbanner") } }
            },
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
    fun `media from scope context`() {
        execute(
            """
            .if {yes}
                ![Quarkdown](img/icon.png)                                 
            """.trimIndent(),
            enableMediaStorage = true,
            outputResourceHook = { group ->
                assertTrue(getMediaResources(group).single().name.startsWith("icon"))
            },
        ) {
            assertEquals("<figure><img src=\"media/icon", it.toString().substringBefore("@"))
        }
    }

    @Test
    fun `subdocument should have its own media storage`() {
        execute(
            source = "[1](subdoc/media-storage.qd)",
            enableMediaStorage = true,
            outputResourceHook = { group ->
                assertFails { getMediaResources(group) } // Root has no media
                assertNotNull(getMediaResources(group, "media-storage").singleOrNull { it.name.startsWith("icon") })
            },
        ) {
            if (subdocument == Subdocument.Root) {
                assertEquals(0, mediaStorage.all.size)
            } else {
                assertEquals(1, mediaStorage.all.size)
            }
        }
    }

    @Test
    fun `same media from root and subdocument should be separated`() {
        execute(
            source = "![icon](img/icon.png)\n\n[1](subdoc/media-storage.qd)",
            enableMediaStorage = true,
            outputResourceHook = { group ->
                assertNotNull(getMediaResources(group).singleOrNull { it.name.startsWith("icon") })
                assertNotNull(getMediaResources(group, "media-storage").singleOrNull { it.name.startsWith("icon") })
            },
        ) {
            assertEquals(1, mediaStorage.all.size)
            assertContains(it, "<img src=\"media/icon")
        }
    }

    @Test
    fun `subdocument name clashing with media directory, with no root media`() {
        execute(
            source = "[1](subdoc/media.qd)",
            enableMediaStorage = true,
            outputResourceHook = { group ->
                assertNotNull(getMediaResources(group).singleOrNull { it.name == INDEX }) // index.html is the only file in media/
                assertNotNull(getMediaResources(group, "media").singleOrNull { it.name.startsWith("icon") })
            },
        ) {}
    }

    @Test
    fun `subdocument name clashing with media directory, with root media`() {
        execute(
            source = "![icon](img/icon.png)\n\n[1](subdoc/media.qd)",
            enableMediaStorage = true,
            outputResourceHook = { group ->
                val mediaGroups =
                    (group as OutputResourceGroup)
                        .resources
                        .filter { it.name == MEDIA_SUBDIRECTORY_NAME }
                        .filterIsInstance<OutputResourceGroup>()
                assertEquals(2, mediaGroups.size) // Two 'media' subdocuments: root's media and 'media' subdocument

                val flattened = mediaGroups.flatMap { it.resources }

                assertTrue(flattened.any { it.name == INDEX }) // index.html is part of the 'media' subdocument
                assertTrue(flattened.any { it.name.startsWith("icon") }) // root media
                assertNotNull(
                    (
                        flattened.first { it.name == MEDIA_SUBDIRECTORY_NAME } as OutputResourceGroup
                    ).resources.singleOrNull { it.name.startsWith("icon") },
                ) // 'media' subdocument media
            },
        ) {}
    }
}
