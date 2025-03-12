package eu.iamgio.quarkdown.pdf.html

import eu.iamgio.quarkdown.pdf.PdfExportOptions
import eu.iamgio.quarkdown.pdf.PdfExporter
import java.io.File

/**
 *
 */
class HtmlToPdfExporter(
    private val options: PdfExportOptions,
) : PdfExporter {
    override fun export(out: File) {
    }
}
