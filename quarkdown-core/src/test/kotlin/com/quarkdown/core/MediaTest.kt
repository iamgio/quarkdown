package com.quarkdown.core

import com.quarkdown.core.ast.Document
import com.quarkdown.core.ast.attributes.MutableAstAttributes
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.media.getStoredMedia
import com.quarkdown.core.context.hooks.MediaStorerHook
import com.quarkdown.core.media.LocalMedia
import com.quarkdown.core.media.Media
import com.quarkdown.core.media.MediaVisitor
import com.quarkdown.core.media.RemoteMedia
import com.quarkdown.core.media.ResolvableMedia
import com.quarkdown.core.media.storage.MutableMediaStorage
import com.quarkdown.core.media.storage.options.ReadOnlyMediaStorageOptions
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResourceGroup
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
 * Test for the media storage and media resolution.
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

        assertIs<LocalMedia>(ResolvableMedia("src/test/resources/media/icon.png").accept(selfVisitor))
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
        val iconLink =
            Link(
                label = listOf(Text("label")),
                url = "media/icon.png",
                title = null,
            )

        val logoLink =
            Link(
                label = listOf(Text("label")),
                url = "https://iamgio.eu/quarkdown/img/logo-light.svg",
                title = null,
            )

        val tree =
            Document(
                listOf(
                    Paragraph(
                        listOf(
                            Text("abc"),
                            Image(
                                iconLink,
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
                                            logoLink,
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

        val attributes = MutableAstAttributes()

        // The storage is updated while traversing the tree.
        ObservableAstIterator()
            .attach(MediaStorerHook(storage, attributes, workingDirectory = File("src/test/resources")))
            .traverse(tree)

        assertEquals(2, storage.all.size)

        storage.resolve("media/icon.png")?.let { resolved ->
            assertTrue(resolved.name.startsWith("icon@"))
            assertTrue(resolved.name.endsWith(".png"))
            assertTrue(resolved.path.startsWith("media/icon@"))
            assertTrue(resolved.name.endsWith(".png"))

            assertEquals(resolved, iconLink.getStoredMedia(attributes))
        }

        storage.resolve("https://iamgio.eu/quarkdown/img/logo-light.svg")?.let { resolved ->
            assertEquals("https-iamgio.eu-quarkdown-img-logo-light.svg", resolved.name)
            assertTrue(resolved.path.startsWith("media/https-"))
            assertEquals(resolved, logoLink.getStoredMedia(attributes))
        }

        assertNull(storage.resolve("media/other.png"))
        assertNull(tree.getStoredMedia(attributes))
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
            assertEquals(File("src/test/resources/media/icon.png").readBytes().toList(), icon.content)
        }

        resource.resources.first { it.name == "https-iamgio.eu-quarkdown-img-tbanner-light.png" }.let { banner ->
            assertIs<BinaryOutputArtifact>(banner)
            assertEquals(
                URL("https://iamgio.eu/quarkdown/img/tbanner-light.png").readBytes().toList(),
                banner.content,
            )
        }
    }
}
