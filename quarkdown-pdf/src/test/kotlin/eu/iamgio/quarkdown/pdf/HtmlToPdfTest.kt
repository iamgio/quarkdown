package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.interaction.executable.NodeJsWrapper
import eu.iamgio.quarkdown.interaction.executable.NpmWrapper
import eu.iamgio.quarkdown.pdf.html.HtmlPdfExporter
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.Assume.assumeTrue
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private val options =
    PdfExportOptions(
        nodeJsPath = NodeJsWrapper.defaultPath,
        npmPath = NpmWrapper.defaultPath,
    )

/**
 * Tests for HTML-to-PDF generation.
 */
class HtmlToPdfTest {
    private val directory: File =
        kotlin.io.path
            .createTempDirectory()
            .toFile()

    @BeforeTest
    fun setup() {
        directory.deleteRecursively()
        directory.mkdirs()
    }

    @Test
    fun `corresponding exporter`() {
        val html = QuarkdownFlavor.rendererFactory.html(MutableContext(QuarkdownFlavor))
        val htmlToPdf = PdfExporters.getForRenderingTarget(html.postRenderer, options)

        assertIs<HtmlPdfExporter>(htmlToPdf)
    }

    @Test
    fun `bare script on simple html`() {
        assumeTrue(NodeJsWrapper(options.nodeJsPath, workingDirectory = directory).isValid)
        assumeTrue(NpmWrapper(options.npmPath).isValid)

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
                <script>function isReady() { return true; }</script>
            </body>
            </html>
            """.trimIndent(),
        )

        val out = File(directory, "out.pdf")
        HtmlPdfExporter(options.copy(noSandbox = true)).export(directory, out)

        assertTrue(out.exists())
        assertFalse(File(directory, "pdf.js").exists())
        assertFalse(File(directory, "package.json").exists())
        assertFalse(File(directory, "package-lock.json").exists())
        println(directory)
        assertFalse(File(directory, "node_modules").exists())

        Loader.loadPDF(out).use {
            val text = PDFTextStripper().getText(it).trim()
            assertEquals(1, it.numberOfPages)
            assertEquals("Hello, Quarkdown!", text)
        }
    }
}
