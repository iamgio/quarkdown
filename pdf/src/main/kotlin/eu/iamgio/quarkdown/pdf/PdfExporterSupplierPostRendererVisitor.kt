package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.pdf.html.HtmlToPdfExporter
import eu.iamgio.quarkdown.rendering.PostRendererVisitor
import eu.iamgio.quarkdown.rendering.html.HtmlPostRenderer

/**
 * Visitor that supplies [PdfExporter]s for each kind of [PostRenderer].
 */
class PdfExporterSupplierPostRendererVisitor : PostRendererVisitor<PdfExporter> {
    override fun visit(postRenderer: HtmlPostRenderer) = HtmlToPdfExporter()
}
