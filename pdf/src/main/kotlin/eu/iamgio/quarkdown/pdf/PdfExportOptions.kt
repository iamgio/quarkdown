package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper

/**
 * Options for exporting PDF files.
 */
data class PdfExportOptions(
    val nodeJsPath: String = NodeJsWrapper.DEFAULT_PATH,
    val npmPath: String = NpmWrapper.DEFAULT_PATH,
)
