package com.quarkdown.cli

import com.quarkdown.cli.exec.runQuarkdown
import com.quarkdown.cli.exec.strategy.ReplExecutionStrategy
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.session.QuarkdownSession
import com.quarkdown.rendering.html.HtmlExportOptions
import com.quarkdown.rendering.html.extension.html
import com.quarkdown.stdlib.Stdlib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the REPL execution strategy, fed with scripted input lines
 * and observed through the rendered output of each iteration.
 */
class ReplExecutionStrategyTest {
    private val outputs = mutableListOf<String>()

    private val session =
        QuarkdownSession(
            Pipeline(
                context = MutableContext(),
                options = PipelineOptions(enableMediaStorage = false),
                libraries = LibraryExporter.exportAll(Stdlib),
                renderer = { factory, context -> factory.html(context, HtmlExportOptions(resourcesLayout = null)) },
                hooks = PipelineHooks(afterAllRendering = { outputs += it.toString() }),
            ),
        )

    private fun repl(vararg lines: String) = ReplExecutionStrategy(lines.iterator().let { { if (it.hasNext()) it.next() else null } })

    private fun run(vararg lines: String) =
        session.createContext().use { context ->
            repl(*lines).execute(session.createPipeline(context))
        }

    @Test
    fun `exits on exit keyword without executing it`() {
        assertNull(run("exit"))
        assertTrue(outputs.isEmpty())
    }

    @Test
    fun `exits when the input is exhausted`() {
        assertNull(run("Hello"))
        assertEquals(1, outputs.size)
    }

    @Test
    fun `renders every input`() {
        run("# Title", "Paragraph", "exit")
        assertEquals(2, outputs.size)
        assertTrue("<h1" in outputs[0])
        assertTrue("<p>Paragraph</p>" in outputs[1])
    }

    @Test
    fun `definitions persist across inputs`() {
        run(".var {x} {5}", ".function {twice}\n    n:\n    .multiply {.n} by:{2}", ".twice {.x}", "exit")
        assertTrue("<p>10</p>" in outputs.last())
    }

    @Test
    fun `runQuarkdown closes the context after the loop`() {
        val cliOptions =
            CliOptions(
                source = null,
                outputDirectory = null,
                libraryDirectory = null,
                rendererName = "html",
                clean = false,
                pipe = false,
                chromePath = "unused",
            )

        val outcome = runQuarkdown(repl(".var {x} {5}", ".x", "exit"), session, cliOptions)

        assertNull(outcome.resource)
        assertNull(outcome.directory)
        assertNull(outcome.context.attachedPipeline)
        assertTrue("<p>5</p>" in outputs.last())
    }
}
