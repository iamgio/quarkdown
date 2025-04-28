package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.DEFAULT_OPTIONS
import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the media storage system.
 */
class MediaStorageTest {
    @Test
    fun `media storage`() {
        execute(
            """
            This is the Quarkdown logo: ![Quarkdown](img/icon.png).                                 
            """.trimIndent(),
            enableMediaStorage = false,
        ) {
            assertEquals("<p>This is the Quarkdown logo: <img src=\"img/icon.png\" alt=\"Quarkdown\" />.</p>", it)
            assertEquals(0, mediaStorage.all.size)
        }

        execute(
            """
            This is the Quarkdown logo: ![Quarkdown](img/icon.png).                                 
            """.trimIndent(),
            enableMediaStorage = true,
        ) {
            assertEquals("<p>This is the Quarkdown logo: <img src=\"media/icon", it.toString().substringBefore("@"))
            // The file name is "media/icon-[encoded].png"
            assertEquals("\" alt=\"Quarkdown\" />.</p>", it.toString().substringAfter(".png"))
        }

        execute(
            """
            .container
                ![Icon](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/ticon-light.svg "The Quarkdown icon")
                
                ![Banner](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg)
            """.trimIndent(),
            options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = true),
            enableMediaStorage = true,
        ) {
            assertEquals(
                "<div class=\"container\">" +
                    "<figure>" +
                    "<img src=\"media/https-raw.githubusercontent.com-iamgio-quarkdown-project-files-images-ticon-light.svg\" " +
                    "alt=\"Icon\" title=\"The Quarkdown icon\" />" +
                    "<figcaption>The Quarkdown icon</figcaption>" +
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
}
