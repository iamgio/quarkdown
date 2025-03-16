package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper

/**
 * Options for exporting PDF files.
 * @param nodeJsPath path to Node.js executable
 * @param npmPath path to NPM executable
 */
data class PdfExportOptions(
    val nodeJsPath: String = NodeJsWrapper.DEFAULT_PATH,
    val npmPath: String = NpmWrapper.DEFAULT_PATH,
)
