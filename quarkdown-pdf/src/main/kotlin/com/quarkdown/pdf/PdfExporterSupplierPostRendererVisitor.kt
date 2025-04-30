package com.quarkdown.pdf

import com.quarkdown.core.rendering.PostRendererVisitor
import com.quarkdown.core.rendering.html.HtmlPostRenderer
import com.quarkdown.pdf.html.HtmlPdfExporter

/**
 * Visitor that supplies [PdfExporter]s for each kind of [PostRenderer].
 * For example, an [HtmlPostRenderer] will produce an [com.quarkdown.pdf.html.HtmlPdfExporter].
 */
class PdfExporterSupplierPostRendererVisitor(
    private val options: PdfExportOptions,
) : PostRendererVisitor<PdfExporter> {
    override fun visit(postRenderer: HtmlPostRenderer) = HtmlPdfExporter(options)
}
