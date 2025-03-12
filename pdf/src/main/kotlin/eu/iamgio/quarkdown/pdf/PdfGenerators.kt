package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.rendering.PostRenderer
import eu.iamgio.quarkdown.rendering.RenderingComponents

/**
 * Utilities for [PdfGenerator]s.
 */
object PdfGenerators {
    private fun getForRenderingTarget(postRenderer: PostRenderer) = postRenderer.accept(PdfGeneratorSupplierPostRendererVisitor())

    /**
     * @param components rendering components to get the [PdfGenerator] for
     * @return the corresponding [PdfGenerator] that can work on the output of the given components
     */
    fun getForRenderingTarget(components: RenderingComponents) = getForRenderingTarget(components.postRenderer)
}
