package com.quarkdown.cli.renderer

import com.quarkdown.cli.CliOptions
import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.rendering.html.HtmlExportOptions
import com.quarkdown.rendering.html.extension.html
import com.quarkdown.rendering.html.pdf.ChromiumExportFormat
import com.quarkdown.rendering.html.pdf.ChromiumExportOptions
import com.quarkdown.rendering.html.pdf.htmlChromiumExport
import com.quarkdown.rendering.markdown.extension.gfm
import com.quarkdown.rendering.plaintext.extension.plainText

/**
 * Name of the default HTML renderer, which [CliOptions.exportPdf] promotes to [HTML_PDF].
 */
private const val HTML = "html"

/**
 * Name of the renderer that exports the HTML output to PDF.
 */
private const val HTML_PDF = "html-pdf"

/**
 * A rendering target for the pipeline, which produces the components to generate the output with.
 */
private typealias Renderer = (RendererFactory, Context) -> RenderingComponents

/**
 * Given a [CliOptions] instance, retrieves the appropriate renderer (e.g. HTML, PDF, PNG) for the pipeline
 * based on [CliOptions.rendererName] (case-insensitive), [CliOptions.exportPdf] and other options.
 */
class RendererRetriever(
    private val options: CliOptions,
) {
    /**
     * Renderers available to the CLI, by name.
     *
     * Note: the current implementation hardcodes renderer names. In the future an extensible retriever will be implemented.
     */
    private val renderers: Map<String, Renderer> =
        mapOf(
            HTML to { factory, context -> factory.html(context, createHtmlExportOptions()) },
            HTML_PDF to chromiumExport(ChromiumExportFormat.Pdf),
            "html-png" to chromiumExport(ChromiumExportFormat.Png),
            "html-webp" to chromiumExport(ChromiumExportFormat.Webp),
            "gfm" to { factory, context -> factory.gfm(context) },
            "markdown" to { factory, context -> factory.gfm(context) },
            "text" to { factory, context -> factory.plainText(context) },
        )

    /**
     * The effective renderer name, where `--pdf` promotes the default HTML renderer to its PDF export.
     */
    private val name: String
        get() =
            options.rendererName.lowercase().let { name ->
                if (name == HTML && options.exportPdf) HTML_PDF else name
            }

    /**
     * Retrieves the rendering target specified by [options].
     * @return the rendering target for the pipeline, to generate the output for
     * @throws IllegalArgumentException if the renderer name is not supported
     */
    fun getRenderer(): Renderer = renderers[name] ?: throw IllegalArgumentException("Unsupported renderer: '${options.rendererName}'")

    /**
     * @param format format to export the HTML output to via a Chromium-family browser
     * @return a renderer that produces HTML and exports it to [format]
     */
    private fun chromiumExport(format: ChromiumExportFormat): Renderer =
        { factory, context ->
            factory.htmlChromiumExport(context, format, createChromiumExportOptions(), createHtmlExportOptions())
        }

    private fun createHtmlExportOptions() = HtmlExportOptions()

    private fun createChromiumExportOptions() =
        ChromiumExportOptions(
            outputDirectory = requireNotNull(options.outputDirectory) { "Output directory must be specified for export." },
            chromePath = options.chromePath,
            noSandbox = options.noPdfSandbox,
        )
}
