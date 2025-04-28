package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.Document
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.inline.Image
import eu.iamgio.quarkdown.ast.base.inline.Link
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.context.hooks.MediaStorerHook
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.media.LocalMedia
import eu.iamgio.quarkdown.media.Media
import eu.iamgio.quarkdown.media.MediaVisitor
import eu.iamgio.quarkdown.media.RemoteMedia
import eu.iamgio.quarkdown.media.ResolvableMedia
import eu.iamgio.quarkdown.media.storage.MutableMediaStorage
import eu.iamgio.quarkdown.media.storage.options.ReadOnlyMediaStorageOptions
import eu.iamgio.quarkdown.pipeline.output.BinaryOutputArtifact
import eu.iamgio.quarkdown.pipeline.output.OutputResourceGroup
import eu.iamgio.quarkdown.rendering.html.QuarkdownHtmlNodeRenderer
import java.io.File
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 *
 */
class MediaTest {
    @Test
    fun `media type resolution`() {
        // A visitor that, when called on a ResolvableMedia, returns the media itself after it has been resolved.
        val selfVisitor =
            object : MediaVisitor<Media> {
                override fun visit(media: LocalMedia) = media

                override fun visit(media: RemoteMedia) = media
            }

        assertIs<LocalMedia>(ResolvableMedia("src/main/resources/render/html-wrapper.html.template").accept(selfVisitor))
        assertIs<RemoteMedia>(ResolvableMedia("https://example.com/image.jpg").accept(selfVisitor))
        assertFails { ResolvableMedia("nonexistent").accept(selfVisitor) }
        assertFails { ResolvableMedia("src").accept(selfVisitor) } // Directory
    }

    @Test
    fun `media storage resolution`() {
        val localOnlyStorage =
            MutableMediaStorage(
                options =
                    ReadOnlyMediaStorageOptions(
                        enableLocalMediaStorage = true,
                        enableRemoteMediaStorage = false,
                    ),
            )

        localOnlyStorage.register("media/icon.png", workingDirectory = File("src/test/resources"))

        assertEquals(1, localOnlyStorage.all.size)

        val stored = localOnlyStorage.all.first()
        assertTrue(stored.name.startsWith("icon@"))
        assertTrue(stored.name.endsWith(".png"))

        localOnlyStorage.resolve("media/icon.png")?.let { resolved ->
            assertEquals(stored, resolved)
            assertEquals(stored.name, resolved.name)
        }

        localOnlyStorage.register("media/banner.png", workingDirectory = File("src/test/resources"))
        assertEquals(2, localOnlyStorage.all.size)

        localOnlyStorage.resolve("media/icon.png")?.let { resolved ->
            assertEquals(stored, resolved)
        }

        localOnlyStorage.resolve("media/banner.png")?.let { resolved ->
            assertTrue(resolved.name.startsWith("banner@"))
            assertTrue(resolved.name.endsWith(".png"))
        }

        val remoteMediaUrl = "https://iamgio.eu/quarkdown/img/logo-light.svg"

        // The storage is local-only, so it doesn't store remote media.
        localOnlyStorage.register(remoteMediaUrl, workingDirectory = null)
        assertEquals(2, localOnlyStorage.all.size)
        assertNull(localOnlyStorage.resolve(remoteMediaUrl))

        val localAndRemoteStorage =
            MutableMediaStorage(
                options =
                    ReadOnlyMediaStorageOptions(
                        enableLocalMediaStorage = true,
                        enableRemoteMediaStorage = true,
                    ),
            )

        localAndRemoteStorage.register("media/icon.png", workingDirectory = File("src/test/resources"))
        localAndRemoteStorage.register(remoteMediaUrl, workingDirectory = null)
        localAndRemoteStorage.register("media/banner.png", workingDirectory = File("src/test/resources"))

        assertEquals(3, localAndRemoteStorage.all.size)

        localAndRemoteStorage.resolve(remoteMediaUrl)?.let { resolved ->
            assertEquals("https-iamgio.eu-quarkdown-img-logo-light.svg", resolved.name)
        }

        localAndRemoteStorage.resolve("media/banner.png")?.let { resolved ->
            assertTrue(resolved.name.startsWith("banner@"))
            assertTrue(resolved.name.endsWith(".png"))
        }

        localAndRemoteStorage.register("media/path1/logo.png", workingDirectory = File("src/test/resources"))
        localAndRemoteStorage.register("media/path2/logo.png", workingDirectory = File("src/test/resources"))

        assertEquals(5, localAndRemoteStorage.all.size)
        assertNotEquals(
            localAndRemoteStorage.resolve("media/path1/logo.png")!!.name,
            localAndRemoteStorage.resolve("media/path2/logo.png")!!.name,
        )
    }

    @Test
    fun `automatic media registration`() {
        val tree =
            Document(
                listOf(
                    Paragraph(
                        listOf(
                            Text("abc"),
                            Image(
                                Link(
                                    label = listOf(Text("label")),
                                    url = "media/icon.png",
                                    title = null,
                                ),
                                width = null,
                                height = null,
                            ),
                        ),
                    ),
                    BlockQuote(
                        children =
                            listOf(
                                Heading(
                                    depth = 1,
                                    listOf(
                                        Image(
                                            Link(
                                                label = listOf(Text("label")),
                                                url = "https://iamgio.eu/quarkdown/img/logo-light.svg",
                                                title = null,
                                            ),
                                            width = null,
                                            height = null,
                                        ),
                                    ),
                                ),
                            ),
                    ),
                ),
            )

        val storage =
            MutableMediaStorage(
                options =
                    ReadOnlyMediaStorageOptions(
                        enableLocalMediaStorage = true,
                        enableRemoteMediaStorage = true,
                    ),
            )

        // The storage is updated while traversing the tree.
        ObservableAstIterator()
            .attach(MediaStorerHook(storage, workingDirectory = File("src/test/resources")))
            .traverse(tree)

        assertEquals(2, storage.all.size)

        storage.resolve("media/icon.png")?.let { resolved ->
            assertTrue(resolved.name.startsWith("icon@"))
            assertTrue(resolved.name.endsWith(".png"))
        }

        storage.resolve("https://iamgio.eu/quarkdown/img/logo-light.svg")?.let { resolved ->
            assertEquals("https-iamgio.eu-quarkdown-img-logo-light.svg", resolved.name)
        }

        assertNull(storage.resolve("media/other.png"))
    }

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

    @Test
    fun `resource exportation`() {
        val storage =
            MutableMediaStorage(
                options =
                    ReadOnlyMediaStorageOptions(
                        enableLocalMediaStorage = true,
                        enableRemoteMediaStorage = true,
                    ),
            )

        storage.register("media/icon.png", workingDirectory = File("src/test/resources"))
        storage.register("https://iamgio.eu/quarkdown/img/tbanner-light.png", workingDirectory = null)
        storage.register("media/banner.png", workingDirectory = File("src/test/resources"))

        val resource = storage.toResource()

        assertIs<OutputResourceGroup>(resource)
        assertEquals(3, resource.resources.size)

        resource.resources.first { it.name.startsWith("icon@") }.let { icon ->
            assertIs<BinaryOutputArtifact>(icon)
            assertEquals(storage.resolve("media/icon.png")?.name, icon.name)
            assertTrue(File("src/test/resources/media/icon.png").readBytes().contentEquals(icon.content))
        }

        resource.resources.first { it.name == "https-iamgio.eu-quarkdown-img-tbanner-light.png" }.let { banner ->
            assertIs<BinaryOutputArtifact>(banner)
            assertTrue(
                URL("https://iamgio.eu/quarkdown/img/tbanner-light.png").readBytes().contentEquals(banner.content),
            )
        }
    }
}
