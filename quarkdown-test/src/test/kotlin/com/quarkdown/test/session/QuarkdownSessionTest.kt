package com.quarkdown.test.session

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.pipeline.session.QuarkdownSession
import com.quarkdown.rendering.html.HtmlExportOptions
import com.quarkdown.rendering.html.extension.html
import com.quarkdown.stdlib.Stdlib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuarkdownSessionTest {
    private fun session() =
        QuarkdownSession(
            Pipeline(
                context = MutableContext(),
                options = PipelineOptions(fileSystem = VirtualFileSystem(), resourceName = "doc"),
                libraries = LibraryExporter.exportAll(Stdlib),
                renderer = { factory, context -> factory.html(context, HtmlExportOptions(resourcesLayout = null)) },
            ),
        )

    /**
     * @return the content of the HTML artifact of this resource tree
     */
    private val OutputResource?.html: String
        get() =
            generateSequence(listOf(this!!)) { level -> level.filterIsInstance<OutputResourceGroup>().flatMap { it.resources } }
                .takeWhile { it.isNotEmpty() }
                .flatten()
                .filterIsInstance<TextOutputArtifact>()
                .single { it.type == ArtifactType.HTML }
                .content
                .toString()

    @Test
    fun `compiles the same session twice`() {
        val session = session()
        val first = session.compile("# Hello\n\n.emoji {wink}").html
        val second = session.compile("# Hello\n\n.emoji {wink}").html

        assertEquals(first, second)
        assertTrue("<h1" in first)
        assertTrue("😉" in first)
    }

    @Test
    fun `each compile starts from a fresh context`() {
        val session = session()
        session.compile(".var {x} {42}")
        val html = session.compile(".x").html
        assertFalse("42" in html.substringAfter("<body"))
    }

    @Test
    fun `shared context keeps definitions across executions`() {
        val session = session()
        session.createContext().use { context ->
            val pipeline = session.createPipeline(context)
            pipeline.execute(".var {x} {42}")
            val html = pipeline.execute(".x").html
            assertTrue("42" in html.substringAfter("<body"))
        }
    }

    @Test
    fun `closing a context detaches its pipeline`() {
        val session = session()
        val context = session.createContext()
        session.createPipeline(context).execute("Hello")
        assertNotNull(context.attachedPipeline)
        context.close()
        assertNull(context.attachedPipeline)
    }
}
