package eu.iamgio.quarkdown.cli

import com.github.ajalt.clikt.testing.test
import eu.iamgio.quarkdown.cli.exec.CompileCommand
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import eu.iamgio.quarkdown.pipeline.error.BasePipelineErrorHandler
import eu.iamgio.quarkdown.pipeline.error.StrictPipelineErrorHandler
import java.io.File
import java.io.FileNotFoundException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for the Quarkdown compile command `c`.
 */
class CompileCommandTest : TempDirectory() {
    private val command = CompileCommand()
    private val main = File(directory, "main.qmd")

    @BeforeTest
    fun setup() {
        super.reset()

        main.writeText(".docname {Quarkdown test}\n\nHello, world!")
    }

    private fun test(additionalArgs: String = ""): Pair<CliOptions, PipelineOptions> {
        command.test(
            "$main " +
                "-o $directory " +
                additionalArgs,
        )

        val outputDir = File(directory, "Quarkdown-test")
        assertTrue(outputDir.exists())
        assertTrue(outputDir.isDirectory())

        outputDir.listFiles()!!.map { it.name }.let {
            "index.html" in it
            "script" in it
            "theme" in it
        }

        val cliOptions = command.createCliOptions()
        val pipelineOptions = command.createPipelineOptions(cliOptions)

        assertEquals(main, cliOptions.source)
        assertEquals(directory, cliOptions.outputDirectory)
        assertEquals(directory, pipelineOptions.workingDirectory)

        return cliOptions to pipelineOptions
    }

    @Test
    fun base() {
        val (cliOptions, pipelineOptions) = test()

        assertFalse(cliOptions.clean)

        pipelineOptions.let {
            assertFalse(it.prettyOutput)
            assertTrue(it.wrapOutput)
            assertTrue(it.enableMediaStorage)
            assertIs<BasePipelineErrorHandler>(it.errorHandler)
        }
    }

    @Test
    fun strict() {
        val (_, pipelineOptions) = test("--strict")
        assertIs<StrictPipelineErrorHandler>(pipelineOptions.errorHandler)
    }

    @Test
    fun `pretty, no wrap`() {
        val (_, pipelineOptions) = test("--pretty --nowrap")
        assertTrue(pipelineOptions.prettyOutput)
        assertFalse(pipelineOptions.wrapOutput)
    }

    @Test
    fun clean() {
        // The output directory is parent to the source file,
        // hence cleaning it will also delete the source file.
        assertFailsWith<FileNotFoundException> {
            test("--clean")
        }
    }

    @Test
    fun pdf() {
        /*
        TODO implement PDF export
        val (_, _) = test("--pdf")
        val pdf = File(directory, "Quarkdown-test.pdf")
        assertTrue(pdf.exists())
         */
    }
}
