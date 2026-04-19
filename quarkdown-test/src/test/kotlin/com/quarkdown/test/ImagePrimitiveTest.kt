package com.quarkdown.test

import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getMediaResources
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for the `.image` primitive function.
 */
class ImagePrimitiveTest {
    @Test
    fun `image without size constraints`() {
        execute(".image {https://example.com/photo.png} label:{A photo}") {
            assertEquals(
                "<figure><img src=\"https://example.com/photo.png\" alt=\"A photo\" /></figure>",
                it,
            )
        }
    }

    @Test
    fun `image with size constraints`() {
        execute(".image {https://example.com/photo.png} label:{A photo} width:{200px} height:{150px}") {
            assertEquals(
                "<figure><img src=\"https://example.com/photo.png\" alt=\"A photo\"" +
                    " style=\"width: 200.0px; height: 150.0px;\" /></figure>",
                it,
            )
        }
    }

    @Test
    fun `image with title becomes caption`() {
        execute(
            ".image {https://example.com/photo.png} label:{A photo}" +
                " title:{My caption} width:{200px} height:{150px}",
        ) {
            assertEquals(
                "<figure><img src=\"https://example.com/photo.png\" alt=\"A photo\" title=\"My caption\"" +
                    " style=\"width: 200.0px; height: 150.0px;\" />" +
                    "<figcaption class=\"caption-bottom\">My caption</figcaption></figure>",
                it,
            )
        }
    }

    @Test
    fun `image without figure wrapping`() {
        execute(
            "Hello .image {https://example.com/icon.png} label:{Icon}" +
                " figure:{no} width:{16px} height:{16px} world",
        ) {
            assertEquals(
                "<p>Hello <img src=\"https://example.com/icon.png\" alt=\"Icon\"" +
                    " style=\"width: 16.0px; height: 16.0px;\" /> world</p>",
                it,
            )
        }
    }

    @Test
    fun `local image stored to media storage`() {
        execute(
            ".image {img/icon.png} label:{Quarkdown}",
            enableMediaStorage = true,
            outputResourceHook = { group ->
                val resource = getMediaResources(group).single()
                assertTrue(resource.name.startsWith("icon"))
                assertIs<FileReferenceOutputArtifact>(resource)
                assertTrue(resource.file.isFile)
            },
        ) {
            assertContains(it, "<figure><img src=\"media/icon")
            assertEquals(1, mediaStorage.all.size)
        }
    }
}
