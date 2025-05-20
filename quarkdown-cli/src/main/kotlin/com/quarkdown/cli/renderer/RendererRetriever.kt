package com.quarkdown.cli.renderer

import com.quarkdown.cli.CliOptions
import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.rendering.html.extension.html
import com.quarkdown.rendering.html.extension.htmlPdf
import com.quarkdown.rendering.html.pdf.HtmlPdfExportOptions

private const val HTML = "html"
private const val HTML_PDF = "html-pdf"

/**
 * Given a [CliOptions] instance, retrieves the appropriate renderer (e.g. HTML, PDF) for the pipeline
 * based on [CliOptions.rendererName] (case-insensitive), [CliOptions.generatePdf] and other options.
 */
class RendererRetriever(
    private val options: CliOptions,
) {
    private val name
        get() = options.rendererName.lowercase()

    /**
     * Retrieves the rendering target specified by [options].
     *
     * Note: the current implementation hardcodes renderer names. In the future an extensible retriever will be implemented.
     * @return the rendering target for the pipeline, to generate the output for.
     */
    fun getRenderer(): (RendererFactory, Context) -> RenderingComponents =
        { factory, context ->
            when {
                isHtmlPdf() -> factory.htmlPdf(context, createHtmlPdfExportOptions())
                isHtml() -> factory.html(context)
                else -> throw IllegalArgumentException("Unsupported renderer: '${options.rendererName}'")
            }
        }

    private fun isHtml() = name == HTML

    private fun isHtmlPdf() = name == HTML_PDF || (name == HTML && options.generatePdf)

    private fun createHtmlPdfExportOptions() =
        HtmlPdfExportOptions(
            outputDirectory = requireNotNull(options.outputDirectory) { "Output directory must be specified for PDF export." },
            nodeJsPath = options.nodePath,
            npmPath = options.npmPath,
            noSandbox = options.noPdfSandbox,
        )
}
