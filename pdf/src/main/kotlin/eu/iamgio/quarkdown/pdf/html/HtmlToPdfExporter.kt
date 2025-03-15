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
        val node = NodeJsWrapper(path = options.nodeJsPath, workingDirectory = out.parentFile)
        if (!node.isValid) {
            throw IllegalStateException("Node.js cannot be found at '${options.nodeJsPath}'")
        }

        val npm = NodeJsWrapper(path = options.npmPath, workingDirectory = out.parentFile)
        if (!npm.isValid) {
            throw IllegalStateException("Npm cannot be found at '${options.npmPath}'")
        }
    }
}
