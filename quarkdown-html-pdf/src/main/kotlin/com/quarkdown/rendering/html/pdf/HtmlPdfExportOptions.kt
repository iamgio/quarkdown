package com.quarkdown.rendering.html.pdf

import java.io.File

/**
 * Options for exporting PDF files from HTML via [ChromiumPdfGeneratorScript].
 * @param outputDirectory directory to save the output PDF in
 * @param chromePath path to the Chromium-family browser executable
 * @param noSandbox whether to disable the Chromium sandbox for PDF export from HTML. Potentially unsafe
 */
data class HtmlPdfExportOptions(
    val outputDirectory: File,
    val chromePath: String,
    val noSandbox: Boolean = false,
)
