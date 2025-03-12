package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.pdf.html.HtmlToPdfExporter
import eu.iamgio.quarkdown.pdf.html.PdfGeneratorScript
import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * Tests for the PDF module.
 */
class PdfTest {
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
        val htmlToPdf = PdfExporters.getForRenderingTarget(html.postRenderer, PdfExportOptions())

        assertIs<HtmlToPdfExporter>(htmlToPdf)
    }

    @Test
    fun `bare script on simple html`() {
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
            </body>
            </html>
            """.trimIndent(),
        )

        val node = NodeJsWrapper(workingDirectory = directory)
        val npm = NpmWrapper()
        val out = File(directory, "out.pdf")

        // todo figure out how to avoid webserver. file:// times out
        PdfGeneratorScript.launch(out, "http://localhost:8089" /*"file://${html.absolutePath}"*/, node, npm)
        println(directory.listFiles()!!.toList())
    }
}
