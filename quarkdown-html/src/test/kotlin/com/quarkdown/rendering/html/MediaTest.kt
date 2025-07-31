package com.quarkdown.rendering.html

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.media.StoredMediaProperty
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.media.storage.MEDIA_SUBDIRECTORY_NAME
import com.quarkdown.core.media.storage.StoredMedia
import com.quarkdown.rendering.html.node.QuarkdownHtmlNodeRenderer
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val WORKING_DIR_PATH = "src/test/resources"
private const val REMOTE_URL = "https://iamgio.eu/quarkdown/img/logo-light.svg"
private const val REMOTE_OUT_NAME = "https-iamgio.eu-quarkdown-img-logo-light.svg"
private const val LOCAL_PATH = "media/icon.png"
private const val OUT_DIR = MEDIA_SUBDIRECTORY_NAME

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
                url = LOCAL_PATH,
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
            "<img src=\"$OUT_DIR/$REMOTE_OUT_NAME\" alt=\"\" />",
            image.accept(renderer),
        )
    }

    @Test
    fun `local media path update`() {
        val media = context.mediaStorage.register(LOCAL_PATH, workingDirectory = File(WORKING_DIR_PATH))!!
        val image = localImage(media)

        assertTrue(image.accept(renderer).startsWith("<img src=\"$OUT_DIR/icon@"))
    }
}
