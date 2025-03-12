package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.pdf.html.HtmlToPdfExporter
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * Tests for the PDF module
 */
class PdfTest {
    @Test
    fun `corresponding exporter`() {
        val html = QuarkdownFlavor.rendererFactory.html(MutableContext(QuarkdownFlavor))
        val htmlToPdf = PdfExporters.getForRenderingTarget(html)

        assertIs<HtmlToPdfExporter>(htmlToPdf)
    }
}
