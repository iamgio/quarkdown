package com.quarkdown.rendering.html.extension

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.rendering.html.pdf.HtmlPdfExportOptions
import com.quarkdown.rendering.html.pdf.PdfHtmlPostRendererDecorator
import com.quarkdown.rendering.html.post.HtmlPostRenderer
import com.quarkdown.rendering.html.post.HtmlSubdocumentPostRenderer
import java.io.File

/**
 * The HTML rendering plug-in produces a browser-compatible document.
 *
 * - The root document comes with a full export which includes themes and scripts, and possibly media resources.
 * - Other subdocuments are exported to lightweight subdirectories, with possibly media resources.
 *
 * @param libraryDirectory filesystem directory containing third-party library files.
 *        If `null`, no third-party libraries are bundled in the output.
 */
fun RendererFactory.html(
    context: Context,
    libraryDirectory: File? = null,
) = RenderingComponents(
    nodeRenderer = accept(HtmlRendererFactoryVisitor(context)),
    postRenderer =
        when (context.subdocument) {
            Subdocument.Root -> HtmlPostRenderer(context, libraryDirectory = libraryDirectory)
            else -> HtmlSubdocumentPostRenderer(context)
        },
)

/**
 * The HTML-PDF rendering plug-in produces a PDF document from the HTML output of [html].
 * The outcome is 1:1 with what would be displayed in a Chrome browser.
 *
 * @param libraryDirectory filesystem directory containing third-party library files.
 *        If `null`, no third-party libraries are bundled in the output.
 */
fun RendererFactory.htmlPdf(
    context: Context,
    options: HtmlPdfExportOptions,
    libraryDirectory: File? = null,
) = RenderingComponents(
    nodeRenderer = accept(HtmlRendererFactoryVisitor(context)),
    postRenderer =
        PdfHtmlPostRendererDecorator(
            HtmlPostRenderer(context, libraryDirectory = libraryDirectory),
            options = options,
        ),
)
