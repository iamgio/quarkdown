package com.quarkdown.cli

import com.quarkdown.cli.exec.ExecutionTimeoutException
import com.quarkdown.cli.exec.runQuarkdown
import com.quarkdown.cli.exec.strategy.FileExecutionStrategy
import com.quarkdown.cli.exec.strategy.PipelineExecutionStrategy
import com.quarkdown.core.UNRESOLVED_REFERENCE_EXIT_CODE
import com.quarkdown.core.filesystem.DiskFileSystem
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.error.PipelineException
import com.quarkdown.core.pipeline.error.StrictPipelineErrorHandler
import com.quarkdown.core.pipeline.output.OutputResource
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Tests for the programmatic pipeline entry point [runQuarkdown].
 *
 * These tests guard the embedding contract: `runQuarkdown` must never terminate the JVM.
 * On success it returns an outcome; on failure it propagates a [PipelineException] so an
 * in-process embedder (LSP, preview server, test harness) can catch and recover.
 */
class ExecuteTest : TempDirectory() {
    private val source = File(directory, "main.qd")
    private val outputDirectory = File(directory, "out")

    @BeforeTest
    fun setup() {
        super.reset()
    }

    private fun cliOptions() =
        CliOptions(
            source = source,
            outputDirectory = outputDirectory,
            libraryDirectory = null,
            rendererName = "html",
            clean = false,
            pipe = false,
            chromePath = "unused",
        )

    private fun pipelineOptions(strict: Boolean) =
        PipelineOptions(
            fileSystem = DiskFileSystem(source.absoluteFile.parentFile),
            enableMediaStorage = false,
            errorHandler = if (strict) StrictPipelineErrorHandler() else PipelineOptions().errorHandler,
        )

    @Test
    fun `runQuarkdown returns an outcome on success`() {
        source.writeText(
            """
            .docname {Programmatic entry test}
            .doctype {plain}

            Hello world.
            """.trimIndent(),
        )

        val outcome = runQuarkdown(FileExecutionStrategy(source), cliOptions(), pipelineOptions(strict = false))

        assertNotNull(outcome.resource)
        assertNotNull(outcome.directory)
    }

    @Test
    fun `runQuarkdown propagates PipelineException in strict mode instead of exiting`() {
        source.writeText(
            """
            .docname {Failing document}
            .doctype {plain}

            .thisFunctionDoesNotExist
            """.trimIndent(),
        )

        val exception =
            assertFailsWith<PipelineException> {
                runQuarkdown(FileExecutionStrategy(source), cliOptions(), pipelineOptions(strict = true))
            }

        assertEquals(UNRESOLVED_REFERENCE_EXIT_CODE, exception.code)
    }

    @Test
    fun `runQuarkdown enforces timeoutSeconds from CliOptions`() {
        val blockingStrategy =
            object : PipelineExecutionStrategy {
                override fun execute(pipeline: Pipeline): OutputResource? {
                    Thread.sleep(60_000)
                    return null
                }
            }

        val cli = cliOptions().copy(timeoutSeconds = 1)

        val exception =
            assertFailsWith<ExecutionTimeoutException> {
                runQuarkdown(blockingStrategy, cli, pipelineOptions(strict = false))
            }

        assertEquals(1, exception.timeoutSeconds)
    }

    @Test
    fun `null timeoutSeconds runs inline without wrapping`() {
        source.writeText("Trivial body.")
        val cli = cliOptions().copy(timeoutSeconds = null)
        // Should complete without a timeout firing.
        val outcome = runQuarkdown(FileExecutionStrategy(source), cli, pipelineOptions(strict = false))
        assertNotNull(outcome.resource)
    }
}
