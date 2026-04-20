package com.quarkdown.core

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.link.getResolvedUrl
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.file.FileSystem
import com.quarkdown.core.context.file.SimpleFileSystem
import com.quarkdown.core.context.hooks.LinkUrlResolverHook
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.pipeline.PipelineOptions
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

private val ROOT_DIR = File("src/test/resources").absoluteFile

/**
 * Tests for relative link path resolution via [LinkUrlResolverHook].
 */
class LinkUrlResolverTest {
    private fun createContext(workingDirectory: File = ROOT_DIR): MutableContext {
        val context =
            object : MutableContext() {
                override val permissions = setOf(Permission.GlobalRead)
            }
        context.attachMockPipeline(
            options = PipelineOptions(workingDirectory = workingDirectory),
        )
        return context
    }

    /**
     * Creates an [Image] with the given [url] and optional [fileSystem].
     */
    private fun image(
        url: String,
        fileSystem: FileSystem? = null,
    ) = Image(
        Link(
            label = buildInline { text("img") },
            url = url,
            title = null,
            fileSystem = fileSystem,
        ),
        width = null,
        height = null,
    )

    /**
     * Creates a [LinkDefinition] with the given [url] and optional [fileSystem].
     */
    private fun linkDefinition(
        url: String,
        fileSystem: FileSystem? = null,
    ) = LinkDefinition(
        label = buildInline { text("def") },
        url = url,
        title = null,
        fileSystem = fileSystem,
    )

    private fun traverse(
        root: Node,
        context: MutableContext,
    ) {
        ObservableAstIterator()
            .attach(LinkUrlResolverHook(context))
            .traverse(root as NestableNode)
    }

    @Test
    fun `no resolution when file system is null`() {
        val context = createContext()
        val img = image("img/icon.png")
        val root = buildBlock { root { +img } }

        traverse(root, context)

        assertEquals("img/icon.png", img.link.getResolvedUrl(context))
    }

    @Test
    fun `no resolution when file system is root`() {
        val context = createContext()
        val rootFs = SimpleFileSystem(ROOT_DIR)

        val img = image("img/icon.png", fileSystem = rootFs)
        val root = buildBlock { root { +img } }

        traverse(root, context)

        // Root file system is skipped: falls back to the original URL.
        assertEquals("img/icon.png", img.link.getResolvedUrl(context))
    }

    @Test
    fun `resolves relative path from branched file system`() {
        val context = createContext()

        // Simulate a subdocument in a child directory.
        val childDir = File(ROOT_DIR, "subdoc")
        val childFs = SimpleFileSystem(ROOT_DIR).branch(childDir)

        val img = image("../img/icon.png", fileSystem = childFs)
        val root = buildBlock { root { +img } }

        traverse(root, context)

        // From ROOT_DIR, the relative path to childDir is "subdoc".
        // Resolving "../img/icon.png" from "subdoc" yields "img/icon.png" after normalization.
        assertEquals("img/icon.png", img.link.getResolvedUrl(context))
    }

    @Test
    fun `resolves same-directory path from branched file system`() {
        val context = createContext()

        val childDir = File(ROOT_DIR, "subdoc")
        val childFs = SimpleFileSystem(ROOT_DIR).branch(childDir)

        val img = image("picture.png", fileSystem = childFs)
        val root = buildBlock { root { +img } }

        traverse(root, context)

        // From ROOT_DIR to childDir is "subdoc", so "picture.png" becomes "subdoc/picture.png".
        assertEquals("subdoc/picture.png", img.link.getResolvedUrl(context))
    }

    @Test
    fun `skips absolute URLs`() {
        val context = createContext()

        val childFs = SimpleFileSystem(ROOT_DIR).branch(File(ROOT_DIR, "subdoc"))
        val img = image("https://example.com/image.png", fileSystem = childFs)
        val root = buildBlock { root { +img } }

        traverse(root, context)

        assertEquals("https://example.com/image.png", img.link.getResolvedUrl(context))
    }

    @Test
    fun `skips absolute file paths`() {
        val context = createContext()

        val childFs = SimpleFileSystem(ROOT_DIR).branch(File(ROOT_DIR, "subdoc"))
        val img = image("/absolute/path/image.png", fileSystem = childFs)
        val root = buildBlock { root { +img } }

        traverse(root, context)

        assertEquals("/absolute/path/image.png", img.link.getResolvedUrl(context))
    }

    @Test
    fun `skips passthrough paths`() {
        val context = createContext()

        val childFs = SimpleFileSystem(ROOT_DIR).branch(File(ROOT_DIR, "subdoc"))
        val img = image("@/img/icon.png", fileSystem = childFs)
        val root = buildBlock { root { +img } }

        traverse(root, context)

        // Passthrough paths are not resolved: the URL is kept as-is.
        assertEquals("@/img/icon.png", img.link.getResolvedUrl(context))
    }

    @Test
    fun `resolves link definition from branched file system`() {
        val context = createContext()

        val childDir = File(ROOT_DIR, "subdoc")
        val childFs = SimpleFileSystem(ROOT_DIR).branch(childDir)

        val def = linkDefinition("../img/icon.png", fileSystem = childFs)
        val root = buildBlock { root { +def } }

        traverse(root, context)

        assertEquals("img/icon.png", def.getResolvedUrl(context))
    }
}
