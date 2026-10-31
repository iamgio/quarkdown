package com.quarkdown.rendering.html.pdf

import com.quarkdown.core.pipeline.error.IOPipelineException
import com.quarkdown.interaction.executable.ChromiumWrapper
import java.io.File

/**
 * Exports a directory with an `index.html` root file to a [ChromiumExportFormat], such as PDF or PNG.
 * This is done via a Chromium-family browser, driven over the Chrome DevTools Protocol.
 * @param options options that affect the export process
 * @param format format to export the document to
 * @see ChromiumWrapper
 */
class HtmlChromiumExporter(
    private val options: ChromiumExportOptions,
    private val format: ChromiumExportFormat,
) {
    /**
     * Exports the document from the given source directory.
     * @param sourcesDirectory the directory containing the HTML source files
     * @param out the output file for the exported document
     * @throws IOPipelineException if the browser is not available or the export fails
     */
    fun export(
        sourcesDirectory: File,
        out: File,
    ) {
        try {
            val browser = ChromiumWrapper(path = options.chromePath)
            ChromiumExportScript(
                sourcesDirectory,
                out,
                format,
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
