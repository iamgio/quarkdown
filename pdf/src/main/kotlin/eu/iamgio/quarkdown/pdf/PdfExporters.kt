package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.rendering.PostRenderer
import eu.iamgio.quarkdown.rendering.RenderingComponents

/**
 * Utilities for [PdfExporter]s.
 */
object PdfExporters {
    private fun getForRenderingTarget(postRenderer: PostRenderer) = postRenderer.accept(PdfExporterSupplierPostRendererVisitor())

    /**
     * @param components rendering components to get the [PdfExporter] for
     * @return the corresponding [PdfExporter] that can work on the output of the given components
     */
    fun getForRenderingTarget(components: RenderingComponents) = getForRenderingTarget(components.postRenderer)
}
