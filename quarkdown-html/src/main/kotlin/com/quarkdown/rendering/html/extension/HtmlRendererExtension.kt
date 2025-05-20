package com.quarkdown.rendering.html.extension

import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.rendering.html.pdf.HtmlPdfExportOptions
import com.quarkdown.rendering.html.pdf.PdfHtmlPostRendererDecorator
import com.quarkdown.rendering.html.post.HtmlPostRenderer

/**
 * The HTML rendering plug-in produces a browser-compatible document.
 */
fun RendererFactory.html(context: Context) =
    RenderingComponents(
        nodeRenderer = accept(HtmlRendererFactoryVisitor(context)),
        postRenderer = HtmlPostRenderer(context),
    )

/**
 * The HTML-PDF rendering plug-in produces a PDF document from the HTML output of [html].
 * The outcome is 1:1 with what would be displayed in a Chrome browser.
 */
fun RendererFactory.htmlPdf(
    context: Context,
    options: HtmlPdfExportOptions,
) = RenderingComponents(
    nodeRenderer = accept(HtmlRendererFactoryVisitor(context)),
    postRenderer =
        PdfHtmlPostRendererDecorator(
            HtmlPostRenderer(context),
            options = options,
        ),
)
