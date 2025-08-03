package com.quarkdown.core

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.MutableAstAttributes
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.media.StoredMediaProperty
import com.quarkdown.core.ast.media.getStoredMedia
import com.quarkdown.core.media.LocalMedia
import com.quarkdown.core.media.Media
import com.quarkdown.core.media.MediaVisitor
import com.quarkdown.core.media.RemoteMedia
import com.quarkdown.core.media.ResolvableMedia
import com.quarkdown.core.media.storage.MEDIA_SUBDIRECTORY_NAME
import com.quarkdown.core.media.storage.MutableMediaStorage
import com.quarkdown.core.media.storage.StoredMedia
import com.quarkdown.core.media.storage.options.ReadOnlyMediaStorageOptions
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val WORKING_DIR_PATH = "src/test/resources"
private const val LOCAL_DIR = MEDIA_SUBDIRECTORY_NAME
private const val LOCAL_ICON = "$LOCAL_DIR/icon.png"
private const val LOCAL_BANNER = "$LOCAL_DIR/banner.png"
private const val REMOTE_IMAGE = "https://example.com/image.jpg"
private const val REMOTE_LOGO = "https://iamgio.eu/quarkdown/img/logo-light.svg"
private const val REMOTE_LOGO_OUT_NAME = "https-iamgio.eu-quarkdown-img-logo-light.svg"
private const val INVALID_PATH = "nonexistent"
private const val OUT_DIR = MEDIA_SUBDIRECTORY_NAME
private const val OUT_PATH_1 = "$OUT_DIR/path1/logo.png"
private const val OUT_PATH_2 = "$OUT_DIR/path2/logo.png"

/**
 * Test for the media storage and media resolution.
 */
class MediaTest {
    private val workingDir = File(WORKING_DIR_PATH)

    private lateinit var attributes: MutableAstAttributes

    @BeforeTest
    fun setUp() {
        attributes = MutableAstAttributes()
    }

    private fun createLocalOnlyStorage(): MutableMediaStorage =
        MutableMediaStorage(
            ReadOnlyMediaStorageOptions(
                enableLocalMediaStorage = true,
                enableRemoteMediaStorage = false,
            ),
        )

    private fun createLocalAndRemoteStorage(): MutableMediaStorage =
        MutableMediaStorage(
            ReadOnlyMediaStorageOptions(
                enableLocalMediaStorage = true,
                enableRemoteMediaStorage = true,
            ),
        )

    private val selfVisitor =
        object : MediaVisitor<Media> {
            override fun visit(media: LocalMedia) = media

            override fun visit(media: RemoteMedia) = media
        }

    @Test
    fun `resolve valid local media`() {
        val media = ResolvableMedia(File(WORKING_DIR_PATH, LOCAL_ICON).path).accept(selfVisitor)
        assertIs<LocalMedia>(media)
    }

    @Test
    fun `resolve valid remote media`() {
        val media = ResolvableMedia(REMOTE_IMAGE).accept(selfVisitor)
        assertIs<RemoteMedia>(media)
    }

    @Test
    fun `throw on invalid path`() {
        assertFails { ResolvableMedia(INVALID_PATH).accept(selfVisitor) }
    }

    @Test
    fun `throw on directory path`() {
        assertFails { ResolvableMedia(WORKING_DIR_PATH).accept(selfVisitor) }
    }

    @Test
    fun `register and resolve local media`() {
        val storage = createLocalOnlyStorage()
        storage.register(LOCAL_ICON, workingDirectory = workingDir)

        val stored = storage.resolve(LOCAL_ICON)
        assertEquals(1, storage.all.size)
        assertTrue(stored!!.name.startsWith("icon@"))
        assertTrue(stored.name.endsWith(".png"))
    }

    @Test
    fun `resolve multiple local media entries`() {
        val storage = createLocalOnlyStorage()
        storage.register(LOCAL_ICON, workingDirectory = workingDir)
        storage.register(LOCAL_BANNER, workingDirectory = workingDir)

        assertEquals(2, storage.all.size)

        val resolved = storage.resolve(LOCAL_BANNER)
        assertTrue(resolved!!.name.startsWith("banner@"))
        assertTrue(resolved.name.endsWith(".png"))
    }

    @Test
    fun `unresolved remote media in local-only storage`() {
        val storage = createLocalOnlyStorage()
        storage.register(REMOTE_LOGO, workingDirectory = null)
        assertEquals(0, storage.resolve(REMOTE_LOGO)?.let { 1 } ?: 0)
    }

    @Test
    fun `register and resolve both local and remote media`() {
        val storage = createLocalAndRemoteStorage()
        storage.register(LOCAL_ICON, workingDir)
        storage.register(LOCAL_BANNER, workingDir)
        storage.register(REMOTE_LOGO, null)

        assertEquals(3, storage.all.size)
        assertEquals(REMOTE_LOGO_OUT_NAME, storage.resolve(REMOTE_LOGO)?.name)
    }

    @Test
    fun `media with same filename but different paths do not collide`() {
        val storage = createLocalAndRemoteStorage()
        storage.register(OUT_PATH_1, workingDir)
        storage.register(OUT_PATH_2, workingDir)

        val name1 = storage.resolve(OUT_PATH_1)!!.name
        val name2 = storage.resolve(OUT_PATH_2)!!.name
        assertNotEquals(name1, name2)
    }

    private fun Node.attach(media: StoredMedia?) {
        if (media == null) return
        attributes.of(this) += StoredMediaProperty(media)
    }

    private fun remoteImage(media: StoredMedia?) =
        Image(
            Link(
                label = listOf(),
                url = REMOTE_LOGO,
                title = null,
            ).apply { attach(media) },
            width = null,
            height = null,
        )

    private fun localImage(media: StoredMedia?) =
        Image(
            Link(
                label = listOf(),
                url = LOCAL_ICON,
                title = null,
            ).apply { attach(media) },
            width = null,
            height = null,
        )

    @Test
    fun `remote media path retrieval`() {
        val storage = createLocalAndRemoteStorage()
        val media = storage.register(REMOTE_LOGO, workingDirectory = null)!!
        val image = remoteImage(media)
        assertEquals("$OUT_DIR/$REMOTE_LOGO_OUT_NAME", image.link.getStoredMedia(attributes)?.path)
    }

    @Test
    fun `local media path retrieval`() {
        val storage = createLocalAndRemoteStorage()
        val media = storage.register(LOCAL_ICON, workingDirectory = workingDir)!!
        val image = localImage(media)
        image.link.getStoredMedia(attributes)?.path?.let {
            assertTrue(it.startsWith("$OUT_DIR/icon@"))
            assertTrue(it.endsWith(".png"))
        }
    }

    @Test
    fun `denied remote media path retrieval`() {
        val storage = createLocalOnlyStorage()
        val media = storage.register(REMOTE_LOGO, workingDirectory = null)
        val image = remoteImage(media)
        assertNull(media)
        assertNull(image.link.getStoredMedia(attributes))
    }
}
