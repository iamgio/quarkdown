package com.quarkdown.rendering.html.pdf

import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.rendering.html.HtmlExportOptions
import com.quarkdown.rendering.html.extension.HtmlRendererFactoryVisitor
import com.quarkdown.rendering.html.post.HtmlPostRenderer

/**
 * The HTML-PDF rendering plug-in produces a PDF document from the HTML output of
 * [com.quarkdown.rendering.html.extension.html].
 * The outcome is 1:1 with what would be displayed in a Chrome browser.
 */
fun RendererFactory.htmlPdf(
    context: Context,
    pdfOptions: HtmlPdfExportOptions,
    htmlOptions: HtmlExportOptions,
) = RenderingComponents(
    nodeRenderer = accept(HtmlRendererFactoryVisitor(context)),
    postRenderer =
        PdfHtmlPostRendererDecorator(
            HtmlPostRenderer(context, htmlOptions.resourcesLayout),
            options = pdfOptions,
        ),
)
