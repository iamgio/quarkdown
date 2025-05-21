package com.quarkdown.rendering.html

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.media.StoredMediaProperty
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.media.storage.StoredMedia
import com.quarkdown.rendering.html.node.QuarkdownHtmlNodeRenderer
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val REMOTE_URL = "https://iamgio.eu/quarkdown/img/logo-light.svg"

/**
 * Tests for media resolution and rendering via the HTML renderer.
 */
class MediaTest {
    private lateinit var context: MutableContext
    private lateinit var renderer: QuarkdownHtmlNodeRenderer

    @BeforeTest
    fun setUp() {
        context = MutableContext(QuarkdownFlavor)
        renderer = QuarkdownHtmlNodeRenderer(context)
        context.options.enableLocalMediaStorage = true
        context.options.enableRemoteMediaStorage = true
    }

    private fun Node.attach(media: StoredMedia?) {
        if (media == null) return
        context.attributes.of(this) += StoredMediaProperty(media)
    }

    private fun remoteImage(media: StoredMedia?) =
        Image(
            Link(
                label = listOf(),
                url = REMOTE_URL,
                title = null,
            ).apply { attach(media) },
            width = null,
            height = null,
        )

    private fun localImage(media: StoredMedia?) =
        Image(
            Link(
                label = listOf(),
                url = "media/icon.png",
                title = null,
            ).apply { attach(media) },
            width = null,
            height = null,
        )

    @Test
    fun `remote media path update`() {
        val media =
            context.mediaStorage.register(REMOTE_URL, workingDirectory = null)!!

        val image = remoteImage(media)

        assertEquals(
            "<img src=\"media/https-iamgio.eu-quarkdown-img-logo-light.svg\" alt=\"\" />",
            image.accept(renderer),
        )
    }

    @Test
    fun `local media path update`() {
        val media = context.mediaStorage.register("media/icon.png", workingDirectory = File("src/test/resources"))!!
        val image = localImage(media)

        assertTrue(image.accept(renderer).startsWith("<img src=\"media/icon@"))
    }

    @Test
    fun `denied remote media path update`() {
        context.options.enableRemoteMediaStorage = false

        val media = context.mediaStorage.register(REMOTE_URL, workingDirectory = null)
        assertNull(media)
        val image = remoteImage(media)

        assertEquals(
            "<img src=\"https://iamgio.eu/quarkdown/img/logo-light.svg\" alt=\"\" />",
            image.accept(renderer),
        )
    }

    @Test
    fun `unregistered local media path update`() {
        val image = localImage(null)

        assertEquals(
            "<img src=\"media/icon.png\" alt=\"\" />",
            image.accept(renderer),
        )
    }
}
