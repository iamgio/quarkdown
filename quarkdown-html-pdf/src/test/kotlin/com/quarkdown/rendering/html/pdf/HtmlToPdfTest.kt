package com.quarkdown.rendering.html.pdf

import com.quarkdown.interaction.executable.ChromiumWrapper
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.Assume.assumeTrue
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for HTML-to-PDF generation.
 */
class HtmlToPdfTest {
    private val directory: File =
        createTempDirectory()
            .toFile()

    private val options =
        HtmlPdfExportOptions(
            outputDirectory = directory,
            chromePath = ChromiumWrapper.defaultPath,
        )

    @BeforeTest
    fun setup() {
        directory.deleteRecursively()
        directory.mkdirs()
    }

    @Test
    fun `export with blank browser path fails gracefully`() {
        val out = File(directory, "out.pdf")
        HtmlPdfExporter(options.copy(chromePath = " ")).export(directory, out)
        assertFalse(out.exists())
    }

    @Test
    fun `bare generation on simple html`() {
        assumeTrue(runCatching { ChromiumWrapper(options.chromePath).isValid }.getOrDefault(false))

        val html = File(directory, "index.html")
        html.writeText(
            """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test</title>
            </head>
            <body>
                <h1>Hello, Quarkdown!</h1>
                <script>
                function isReady() { return true; }
                window.isReady = isReady;
                </script>
            </body>
            </html>
            """.trimIndent(),
        )

        val out = File(directory, "out.pdf")
        HtmlPdfExporter(options.copy(noSandbox = true)).export(directory, out)

        assertTrue(out.exists())

        Loader.loadPDF(out).use {
            val text = PDFTextStripper().getText(it).trim()
            assertEquals(1, it.numberOfPages)
            assertEquals("Hello, Quarkdown!", text)
        }
    }
}
