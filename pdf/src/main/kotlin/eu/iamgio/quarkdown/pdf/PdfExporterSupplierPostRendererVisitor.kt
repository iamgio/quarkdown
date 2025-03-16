package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.pdf.html.HtmlPdfExporter
import eu.iamgio.quarkdown.rendering.PostRendererVisitor
import eu.iamgio.quarkdown.rendering.html.HtmlPostRenderer

/**
 * Visitor that supplies [PdfExporter]s for each kind of [PostRenderer].
 */
class PdfExporterSupplierPostRendererVisitor(
    private val options: PdfExportOptions,
) : PostRendererVisitor<PdfExporter> {
    override fun visit(postRenderer: HtmlPostRenderer) = HtmlPdfExporter(options)
}
