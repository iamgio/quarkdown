package eu.iamgio.quarkdown.pdf.html

import eu.iamgio.quarkdown.pdf.PdfExportOptions
import eu.iamgio.quarkdown.pdf.PdfExporter
import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import java.io.File

/**
 * Exports a PDF from a directory with an `index.html` root file.
 * This is done via the Puppeteer library, invoked through Node.js.
 * @param options options that affect the export process
 * @see NodeJsWrapper
 * @see NpmWrapper
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
        PuppeteerPdfGeneratorScript(directory, out, node, npm, options.noSandbox).launch()
    }
}
