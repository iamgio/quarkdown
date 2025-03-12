package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.pdf.html.HtmlToPdfGenerator
import eu.iamgio.quarkdown.rendering.PostRendererVisitor
import eu.iamgio.quarkdown.rendering.html.HtmlPostRenderer

class PdfGeneratorSupplierPostRendererVisitor : PostRendererVisitor<PdfGenerator> {
    override fun visit(postRenderer: HtmlPostRenderer) = HtmlToPdfGenerator()
}
