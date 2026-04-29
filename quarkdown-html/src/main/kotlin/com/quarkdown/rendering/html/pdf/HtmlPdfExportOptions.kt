package com.quarkdown.rendering.html.pdf

import java.io.File

/**
 * Options for exporting PDF files from HTML via [PuppeteerNodeModule].
 * @param nodeJsPath path to Node.js executable, or `null` for default
 * @param npmPath path to NPM executable, or `null` for default
 * @param noSandbox whether to disable Chrome sandbox for PDF export from HTML. Potentially unsafe
 * @param timeoutMillis per-operation timeout for the headless browser, in milliseconds.
 *                     `0` disables the timeout, suitable for very large documents whose Paged.js
 *                     rendering exceeds the default. Defaults to Puppeteer's 30s default.
 */
data class HtmlPdfExportOptions(
    val outputDirectory: File,
    val nodeJsPath: String,
    val npmPath: String,
    val noSandbox: Boolean = false,
    val timeoutMillis: Int = DEFAULT_TIMEOUT_MILLIS,
) {
    companion object {
        /**
         * Default per-operation timeout for the headless browser, matching Puppeteer's own default.
         */
        const val DEFAULT_TIMEOUT_MILLIS: Int = 30_000

        /**
         * Sentinel value that disables the timeout entirely.
         */
        const val NO_TIMEOUT: Int = 0
    }
}
