package com.quarkdown.cli

import com.github.ajalt.clikt.testing.test
import com.quarkdown.cli.exec.CompileCommand
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.core.pipeline.error.StrictPipelineErrorHandler
import com.quarkdown.interaction.Env
import com.quarkdown.interaction.executable.NodeJsWrapper
import com.quarkdown.interaction.executable.NpmWrapper
import com.quarkdown.rendering.html.pdf.PuppeteerNodeModule
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
    private val main = File(directory, "main.qd")

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

        val cliOptions = command.createCliOptions()
        val pipelineOptions = command.createPipelineOptions(cliOptions)

        assertEquals(main, cliOptions.source)
        assertEquals(directory, cliOptions.outputDirectory)
        assertEquals(directory, pipelineOptions.workingDirectory)

        return cliOptions to pipelineOptions
    }

    private fun assertHtmlContentPresent(directoryName: String = "Quarkdown-test") {
        val outputDir = File(directory, directoryName)
        assertTrue(outputDir.exists())
        assertTrue(outputDir.isDirectory())

        outputDir.listFiles()!!.map { it.name }.let {
            "index.html" in it
            "script" in it
            "theme" in it
        }
    }

    private fun base(explicitRenderer: String? = null) {
        val (cliOptions, pipelineOptions) =
            explicitRenderer?.let { test("--render", it) }
                ?: test()

        assertHtmlContentPresent()

        assertFalse(cliOptions.clean)

        pipelineOptions.let {
            assertFalse(it.prettyOutput)
            assertTrue(it.wrapOutput)
            assertTrue(it.enableMediaStorage)
            assertIs<BasePipelineErrorHandler>(it.errorHandler)
        }
    }

    @Test
    fun base() = base(null)

    @Test
    fun `base with explicit html renderer`() = base("html")

    @Test
    fun `explicit output name`() {
        val (_, pipelineOptions) = test("--out-name", "A new name")
        assertEquals("A new name", pipelineOptions.resourceName)
        assertHtmlContentPresent(directoryName = "A-new-name")
    }

    @Test
    fun strict() {
        val (_, pipelineOptions) = test("--strict")
        assertHtmlContentPresent()
        assertIs<StrictPipelineErrorHandler>(pipelineOptions.errorHandler)
    }

    @Test
    fun `pretty, no wrap`() {
        val (_, pipelineOptions) = test("--pretty", "--nowrap")
        assertHtmlContentPresent()
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

    private fun assumePdfEnvironmentInstalled() {
        assumeTrue(Env.npmPrefix != null)
        assumeTrue(Env.nodePath != null)
        val node = NodeJsWrapper(NodeJsWrapper.defaultPath, workingDirectory = directory)
        assumeTrue(node.isValid)
        with(NpmWrapper(NpmWrapper.defaultPath)) {
            assumeTrue(isValid)
            assumeTrue(isInstalled(node, PuppeteerNodeModule))
        }
    }

    private fun checkPdf(
        name: String = "Quarkdown-test.pdf",
        expectedPages: Int = 3,
    ) {
        val pdf = File(directory, name)
        assertTrue(pdf.exists())
        assertFalse(File(directory, "Quarkdown-test").exists())

        Loader.loadPDF(pdf).use {
            assertEquals(expectedPages, it.numberOfPages)
        }
    }

    @Test
    fun pdf() {
        assumePdfEnvironmentInstalled()
        val (_, _) = test("--pdf", "--pdf-no-sandbox")
        checkPdf()
    }

    @Test
    fun `pdf with explicit output name`() {
        assumePdfEnvironmentInstalled()
        val (_, _) = test("--pdf", "--pdf-no-sandbox", "--out-name", "A new name")
        checkPdf(name = "A-new-name.pdf")
    }

    @Test
    fun `single-page pdf`() {
        assumePdfEnvironmentInstalled()
        main.writeText(main.readText().replace("paged", "plain") + "\n\n.repeat {100}\n\t.loremipsum")
        val (_, _) = test("--pdf", "--pdf-no-sandbox")
        checkPdf(expectedPages = 1)
    }

    // #86
    @Test
    fun `pdf with toc and id starting with digit`() {
        assumePdfEnvironmentInstalled()
        main.writeText(
            """
            .docname {Quarkdown test}
            .doctype {paged}
            .doclang {en}

            .tableofcontents
            
            # 1 Test
            """.trimIndent(),
        )

        val (_, _) = test("--pdf", "--pdf-no-sandbox")
        checkPdf(expectedPages = 2)
    }

    @Test
    fun `pdf via explicit html-pdf`() {
        assumePdfEnvironmentInstalled()
        val (_, _) = test("--render", "html-pdf", "--pdf-no-sandbox")
        checkPdf()
    }

    @Test
    fun `pdf with node and npm set`() {
        assumePdfEnvironmentInstalled()
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
