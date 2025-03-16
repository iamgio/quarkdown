package eu.iamgio.quarkdown.pdf.html

import eu.iamgio.quarkdown.pdf.PdfExportOptions
import eu.iamgio.quarkdown.pdf.PdfExporter
import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import java.io.File

/**
 *
 */
class HtmlPdfExporter(
    private val options: PdfExportOptions,
) : PdfExporter {
    override fun export(
        directory: File,
        out: File,
    ) {
        val node = NodeJsWrapper(path = options.nodeJsPath, workingDirectory = out.parentFile)
        val npm = NpmWrapper(path = options.npmPath)
        PdfGeneratorScript(directory, out, node, npm).launch()
    }
}
