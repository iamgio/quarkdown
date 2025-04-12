package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.interaction.executable.NodeJsWrapper
import eu.iamgio.quarkdown.interaction.executable.NpmWrapper

/**
 * Options for exporting PDF files.
 * @param nodeJsPath path to Node.js executable
 * @param npmPath path to NPM executable
 * @param noSandbox whether to disable Chrome sandbox for PDF export from HTML. Potentially unsafe
 */
data class PdfExportOptions(
    val nodeJsPath: String = NodeJsWrapper.DEFAULT_PATH,
    val npmPath: String = NpmWrapper.DEFAULT_PATH,
    val noSandbox: Boolean = false,
)
