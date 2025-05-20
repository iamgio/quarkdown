package com.quarkdown.rendering.html.pdf

import java.io.File

/**
 * Options for exporting PDF files from HTML via [PuppeteerNodeModule].
 * @param nodeJsPath path to Node.js executable, or `null` for default
 * @param npmPath path to NPM executable, or `null` for default
 * @param noSandbox whether to disable Chrome sandbox for PDF export from HTML. Potentially unsafe
 */
data class HtmlPdfExportOptions(
    val outputDirectory: File,
    val nodeJsPath: String,
    val npmPath: String,
    val noSandbox: Boolean = false,
)
