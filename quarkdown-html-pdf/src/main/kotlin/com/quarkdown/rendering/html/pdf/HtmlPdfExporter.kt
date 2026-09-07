package com.quarkdown.rendering.html.pdf

import com.quarkdown.core.pipeline.error.IOPipelineException
import com.quarkdown.interaction.executable.ChromiumWrapper
import java.io.File

/**
 * Exports a PDF from a directory with an `index.html` root file.
 * This is done via a Chromium-family browser, driven over the Chrome DevTools Protocol.
 * @param options options that affect the export process
 * @see ChromiumWrapper
 */
class HtmlPdfExporter(
    private val options: HtmlPdfExportOptions,
) {
    /**
     * Exports a PDF from the given source directory.
     * @param sourcesDirectory the directory containing the HTML source files
     * @param out the output file for the generated PDF
     * @throws IOPipelineException if the browser is not available or the export fails
     */
    fun export(
        sourcesDirectory: File,
        out: File,
    ) {
        try {
            val browser = ChromiumWrapper(path = options.chromePath)
            ChromiumPdfGeneratorScript(
                sourcesDirectory,
                out,
                browser,
                options.noSandbox,
            ).launch()
        } catch (e: IllegalArgumentException) {
            throw IOPipelineException("Invalid Chrome path: ${e.message}")
        } catch (e: IllegalStateException) {
            throw IOPipelineException(e.message ?: "Chrome executable is not available")
        }
    }
}
