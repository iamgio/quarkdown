package com.quarkdown.cli

import com.github.ajalt.clikt.testing.test
import com.quarkdown.cli.exec.CompileCommand
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.core.pipeline.error.StrictPipelineErrorHandler
import com.quarkdown.interaction.executable.NodeJsWrapper
import com.quarkdown.interaction.executable.NpmWrapper
import org.apache.pdfbox.Loader
import org.junit.Assume.assumeTrue
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

        main.writeText(
            """
            .docname {Quarkdown test}
            .doctype {paged}

            Page 1
            
            <<<
            
            Page 2
            
            <<<
            
            Page 3
            """.trimIndent(),
        )
    }

    private fun test(vararg additionalArgs: String): Pair<CliOptions, PipelineOptions> {
        command.test(
            main.absolutePath,
            "-o",
            directory.absolutePath,
            *additionalArgs,
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
        val (_, pipelineOptions) = test("--pretty", "--nowrap")
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

    private fun checkPdf() {
        val pdf = File(directory, "Quarkdown-test.pdf")
        assertTrue(pdf.exists())

        Loader.loadPDF(pdf).use {
            assertEquals(3, it.numberOfPages)
        }
    }

    @Test
    fun pdf() {
        assumeTrue(NodeJsWrapper(NodeJsWrapper.defaultPath, workingDirectory = directory).isValid)
        assumeTrue(NpmWrapper(NpmWrapper.defaultPath).isValid)

        val (_, _) = test("--pdf", "--pdf-no-sandbox")
        checkPdf()
    }

    @Test
    fun `pdf with node and npm set`() {
        assumeTrue(NodeJsWrapper(NodeJsWrapper.defaultPath, workingDirectory = directory).isValid)
        assumeTrue(NpmWrapper(NpmWrapper.defaultPath).isValid)

        val (_, _) =
            test(
                "--pdf",
                "--pdf-no-sandbox",
                "--node-path",
                NodeJsWrapper.defaultPath,
                "--npm-path",
                NpmWrapper.defaultPath,
            )
        checkPdf()
    }
}
