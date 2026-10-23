package com.quarkdown.rendering.html.pdf

import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.rendering.html.HtmlExportOptions
import com.quarkdown.rendering.html.extension.HtmlRendererFactoryVisitor
import com.quarkdown.rendering.html.post.HtmlPostRenderer

/**
 * The HTML-Chromium rendering plug-in produces a file in the given [format] (e.g. PDF, PNG, WebP)
 * from the HTML output of [com.quarkdown.rendering.html.extension.html].
 * The outcome is 1:1 with what would be displayed in a Chrome browser.
 * @param format format to export the document to
 * @param exportOptions options that affect the export process
 * @param htmlOptions options that affect the underlying HTML rendering
 */
fun RendererFactory.htmlChromiumExport(
    context: Context,
    format: ChromiumExportFormat,
    exportOptions: ChromiumExportOptions,
    htmlOptions: HtmlExportOptions,
) = RenderingComponents(
    nodeRenderer = accept(HtmlRendererFactoryVisitor(context)),
    postRenderer =
        ChromiumExportHtmlPostRendererDecorator(
            HtmlPostRenderer(context, htmlOptions.resourcesLayout),
            options = exportOptions,
            format = format,
        ),
)
