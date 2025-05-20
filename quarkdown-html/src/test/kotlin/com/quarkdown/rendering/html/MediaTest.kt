package com.quarkdown.rendering.html

import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.rendering.html.node.QuarkdownHtmlNodeRenderer
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for media resolution and rendering via the HTML renderer.
 */
class MediaTest {
    @Test
    fun `path update`() {
        val context = MutableContext(QuarkdownFlavor)
        context.options.enableLocalMediaStorage = true
        context.options.enableRemoteMediaStorage = true

        val renderer = QuarkdownHtmlNodeRenderer(context)

        context.mediaStorage.register("https://iamgio.eu/quarkdown/img/logo-light.svg", workingDirectory = null)

        val remoteImage =
            Image(
                Link(
                    label = listOf(),
                    url = "https://iamgio.eu/quarkdown/img/logo-light.svg",
                    title = null,
                ),
                width = null,
                height = null,
            )

        assertEquals(
            "<img src=\"media/https-iamgio.eu-quarkdown-img-logo-light.svg\" alt=\"\" />",
            remoteImage.accept(renderer),
        )

        context.mediaStorage.register("media/icon.png", workingDirectory = File("src/test/resources"))

        val localImage =
            Image(
                Link(
                    label = listOf(),
                    url = "media/icon.png",
                    title = null,
                ),
                width = null,
                height = null,
            )

        assertTrue(localImage.accept(renderer).startsWith("<img src=\"media/icon@"))

        MutableContext(QuarkdownFlavor).let { localOnlyContext ->
            localOnlyContext.options.enableLocalMediaStorage = true
            localOnlyContext.options.enableRemoteMediaStorage = false

            localOnlyContext.mediaStorage.register(
                "https://iamgio.eu/quarkdown/img/logo-light.svg",
                workingDirectory = null,
            )

            val localOnlyRenderer = QuarkdownHtmlNodeRenderer(localOnlyContext)

            assertEquals(
                "<img src=\"https://iamgio.eu/quarkdown/img/logo-light.svg\" alt=\"\" />",
                remoteImage.accept(localOnlyRenderer),
            )

            // Not yet registered.

            assertEquals(
                "<img src=\"media/icon.png\" alt=\"\" />",
                localImage.accept(localOnlyRenderer),
            )

            localOnlyContext.mediaStorage.register("media/icon.png", workingDirectory = File("src/test/resources"))

            assertTrue(localImage.accept(localOnlyRenderer).startsWith("<img src=\"media/icon@"))
        }
    }
}
