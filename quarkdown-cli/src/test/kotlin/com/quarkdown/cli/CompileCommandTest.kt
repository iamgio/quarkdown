package com.quarkdown.cli

import com.github.ajalt.clikt.testing.CliktCommandTestResult
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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private const val DEFAULT_OUTPUT_DIRECTORY_NAME = "Quarkdown-test"

/**
 * Tests for the Quarkdown compile command `c`.
 */
class CompileCommandTest : TempDirectory() {
    private val command = CompileCommand()
    private val main = File(directory, "main.qd")
    private val outputDirectory = File(directory, "out")

    private val content =
        """
        .docname {Quarkdown test}
        .doctype {paged}

        Page 1
        
        <<<
        
        Page 2
        
        <<<
        
        Page 3
        """.trimIndent()

    @BeforeTest
    fun setup() {
        super.reset()
        main.writeText(content)
    }

    private fun test(vararg additionalArgs: String): Triple<CliOptions, PipelineOptions, CliktCommandTestResult> {
        val result =
            command.test(
                main.absolutePath,
                "-o",
                outputDirectory.absolutePath,
                *additionalArgs,
            )

        val cliOptions = command.createCliOptions()
        val pipelineOptions = command.createPipelineOptions(cliOptions)

        assertEquals(main, cliOptions.source)
        assertEquals(directory, pipelineOptions.workingDirectory)
        assertEquals(
            outputDirectory.takeUnless { cliOptions.pipe },
            cliOptions.outputDirectory,
        )

        return Triple(cliOptions, pipelineOptions, result)
    }

    private fun assertHtmlContentPresent(directoryName: String = DEFAULT_OUTPUT_DIRECTORY_NAME) {
        val outputDir = File(outputDirectory, directoryName)
        assertTrue(outputDir.exists())
        assertTrue(outputDir.isDirectory())

        outputDir.listFiles()!!.map { it.name }.let {
            "index.html" in it
            "script" in it
            "theme" in it
        }
    }

    private fun subdocumentExists(name: String): Boolean =
        outputDirectory
            .resolve(DEFAULT_OUTPUT_DIRECTORY_NAME)
            .resolve(name)
            .let { it.exists() && it.isDirectory() }

    private fun base(explicitRenderer: String? = null) {
        val (cliOptions, pipelineOptions) =
            explicitRenderer?.let { test("--render", it) }
                ?: test()

        assertHtmlContentPresent()

        assertFalse(cliOptions.clean)
        assertFalse(cliOptions.pipe)

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
        val dummyFile =
            File(outputDirectory, "dummy.txt").apply {
                parentFile.mkdirs()
                writeText("This is a dummy file.")
            }
        test("--clean")
        assertHtmlContentPresent()
        assertFalse(dummyFile.exists())
    }

    @Test
    fun pipe() {
        val pipeStdout = java.io.ByteArrayOutputStream()
        val nonPipeStdout = java.io.ByteArrayOutputStream()
        val originalOut = System.out

        try {
            System.setOut(java.io.PrintStream(pipeStdout))
            val (cliOptions, pipelineOptions) = test("--pipe")
            assertTrue(cliOptions.pipe)
            assertTrue(pipelineOptions.wrapOutput)

            val output = pipeStdout.toString()
            assertTrue(output.contains("<!DOCTYPE html>"))
            assertTrue(output.contains("Page 1"))
            assertTrue(output.contains("Page 2"))
            assertTrue(output.contains("Page 3"))
            assertFalse(outputDirectory.exists())

            System.setOut(java.io.PrintStream(nonPipeStdout))
            test()
            val outputNonPipe = nonPipeStdout.toString()
            assertFalse(outputNonPipe.contains("Page 1"))
            assert(output.length > outputNonPipe.length)
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `pipe, no wrap`() {
        val pipeStdout = java.io.ByteArrayOutputStream()
        val originalOut = System.out

        try {
            System.setOut(java.io.PrintStream(pipeStdout))
            val (cliOptions, pipelineOptions) = test("--pipe", "--nowrap")
            assertTrue(cliOptions.pipe)
            assertFalse(pipelineOptions.wrapOutput)

            val output = pipeStdout.toString()
            assertFalse(output.contains("<!DOCTYPE html>"))
            assertTrue(output.contains("Page 1"))
            assertTrue(output.contains("Page 2"))
            assertTrue(output.contains("Page 3"))
        } finally {
            System.setOut(originalOut)
        }
    }

    private fun setupSubdocuments(): List<File> {
        main.writeText("$content\n\n[Subdoc 1](subdoc1.qd)\n\n[Subdoc 2](subdoc2.qd)")

        return listOf(
            File(directory, "subdoc1.qd").apply {
                writeText("This is a subdocument.")
            },
            File(directory, "subdoc2.qd").apply {
                writeText("This is another subdocument. [Subdoc 3](subdoc3.qd)")
            },
            File(directory, "subdoc3.qd").apply {
                writeText("This is yet another subdocument.")
            },
        )
    }

    @Test
    fun `with subdocument`() {
        setupSubdocuments()

        test()
        assertHtmlContentPresent()
        assertTrue(subdocumentExists("subdoc1"))
        assertTrue(subdocumentExists("subdoc2"))
        assertTrue(subdocumentExists("subdoc3"))
    }

    @Test
    fun `with subdocument with minimized collisions`() {
        val (subdoc1, subdoc2, subdoc3) = setupSubdocuments()

        fun assertSubdocumentExistsWithHash(
            name: String,
            file: File,
        ) {
            assertTrue(
                subdocumentExists("$name@${file.absolutePath.hashCode()}") ||
                    subdocumentExists("$name@${file.canonicalFile.absolutePath.hashCode()}"),
            )
        }

        test("--no-subdoc-collisions")
        assertHtmlContentPresent()
        assertSubdocumentExistsWithHash("subdoc1", subdoc1)
        assertSubdocumentExistsWithHash("subdoc2", subdoc2)
        assertSubdocumentExistsWithHash("subdoc3", subdoc3)
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
        name: String = "$DEFAULT_OUTPUT_DIRECTORY_NAME.pdf",
        expectedPages: Int = 3,
    ) {
        val pdf = File(outputDirectory, name)
        assertTrue(pdf.exists())
        assertFalse(File(outputDirectory, DEFAULT_OUTPUT_DIRECTORY_NAME).exists())

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

    @Test
    fun `clean pdf`() {
        assumePdfEnvironmentInstalled()
        test("--pdf", "--pdf-no-sandbox", "--clean")
        checkPdf()
    }

    @Test
    fun `pdf with subdocuments`() {
        assumePdfEnvironmentInstalled()
        setupSubdocuments()
        val (_, _) = test("--pdf", "--pdf-no-sandbox")

        val pdfDir = File(outputDirectory, DEFAULT_OUTPUT_DIRECTORY_NAME)
        assertTrue(pdfDir.exists())
        assertTrue(pdfDir.isDirectory)
        assertTrue(pdfDir.resolve("subdoc1.pdf").exists())
        assertTrue(pdfDir.resolve("subdoc2.pdf").exists())
        assertTrue(pdfDir.resolve("subdoc3.pdf").exists())
        assertEquals(4, pdfDir.listFiles()!!.size)
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

    @Test
    fun `plaintext, single subdocument`() {
        val (_, _, _) = test("--render", "text")
        val outputFile = outputDirectory.resolve("$DEFAULT_OUTPUT_DIRECTORY_NAME.txt")
        assertTrue(outputFile.exists())
        val outputContent = outputFile.readText()
        assertTrue(outputContent.contains("Page 1"))
        assertTrue(outputContent.contains("Page 2"))
        assertTrue(outputContent.contains("Page 3"))
    }

    @Test
    fun `plaintext, multiple subdocuments`() {
        setupSubdocuments()
        val (_, _, _) = test("--render", "text")
        val outputDir = outputDirectory.resolve(DEFAULT_OUTPUT_DIRECTORY_NAME)
        assertTrue(outputDir.exists())
        assertTrue(outputDir.isDirectory)

        val mainOutputFile = outputDir.resolve("index.txt")
        assertTrue(mainOutputFile.exists())
        val mainOutputContent = mainOutputFile.readText()
        assertTrue(mainOutputContent.contains("Page 1"))
        assertTrue(mainOutputContent.contains("Page 2"))
        assertTrue(mainOutputContent.contains("Page 3"))

        val subdoc1OutputFile = outputDir.resolve("subdoc1.txt")
        assertTrue(subdoc1OutputFile.exists())
        val subdoc1OutputContent = subdoc1OutputFile.readText()
        assertTrue(subdoc1OutputContent.contains("This is a subdocument."))

        val subdoc2OutputFile = outputDir.resolve("subdoc2.txt")
        assertTrue(subdoc2OutputFile.exists())
        val subdoc2OutputContent = subdoc2OutputFile.readText()
        assertTrue(subdoc2OutputContent.contains("This is another subdocument."))

        val subdoc3OutputFile = outputDir.resolve("subdoc3.txt")
        assertTrue(subdoc3OutputFile.exists())
        val subdoc3OutputContent = subdoc3OutputFile.readText()
        assertTrue(subdoc3OutputContent.contains("This is yet another subdocument."))
    }
}
