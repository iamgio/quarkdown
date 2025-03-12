package eu.iamgio.quarkdown.pdf.html

import eu.iamgio.quarkdown.pdf.PdfExportOptions
import eu.iamgio.quarkdown.pdf.PdfExporter
import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import java.io.File

/**
 *
 */
class HtmlToPdfExporter(
    private val options: PdfExportOptions,
) : PdfExporter {
    override fun export(out: File) {
        val wrapper = NodeJsWrapper(path = options.nodeJsPath)
        if (!wrapper.isValid) {
            throw IllegalStateException("Node.js cannot be found at '${options.nodeJsPath}'")
        }
    }
}
