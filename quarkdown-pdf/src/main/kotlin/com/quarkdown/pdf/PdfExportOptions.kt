package com.quarkdown.pdf

/**
 * Options for exporting PDF files.
 * @param nodeJsPath path to Node.js executable, or `null` for default
 * @param npmPath path to NPM executable, or `null` for default
 * @param noSandbox whether to disable Chrome sandbox for PDF export from HTML. Potentially unsafe
 */
data class PdfExportOptions(
    val nodeJsPath: String,
    val npmPath: String,
    val noSandbox: Boolean = false,
)
