package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.pdf.html.HtmlToPdfExporter
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
}
