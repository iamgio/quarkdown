package com.quarkdown.rendering.html.pdf

import com.quarkdown.interaction.executable.NodeJsWrapper
import com.quarkdown.interaction.executable.NpmWrapper
import java.io.File

/**
 * Exports a PDF from a directory with an `index.html` root file.
 * This is done via the Puppeteer library, invoked through Node.js.
 * @param options options that affect the export process
 * @see NodeJsWrapper
 * @see NpmWrapper
 */
class HtmlPdfExporter(
    private val options: HtmlPdfExportOptions,
) {
    /**
     * Exports a PDF from the given source directory.
     * @param sourcesDirectory the directory containing the HTML source files
     * @param out the output file for the generated PDF
     */
    fun export(
        sourcesDirectory: File,
        out: File,
    ) {
        val node = NodeJsWrapper(path = options.nodeJsPath, workingDirectory = out.parentFile)
        val npm = NpmWrapper(path = options.npmPath)

        PuppeteerPdfGeneratorScript(
            sourcesDirectory,
            out,
            node,
            npm,
            options.noSandbox,
        ).launch()
    }
}
