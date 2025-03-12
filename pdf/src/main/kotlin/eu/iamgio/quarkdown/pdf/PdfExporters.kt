package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.rendering.PostRenderer

/**
 * Utilities for [PdfExporter]s.
 */
object PdfExporters {
    /**
     * @param postRenderer post-renderer component to get the [PdfExporter] for
     * @param options exporter options
     * @return the corresponding [PdfExporter] that can work on the output of the given post-renderer target
     */
    fun getForRenderingTarget(
        postRenderer: PostRenderer,
        options: PdfExportOptions,
    ) = postRenderer.accept(PdfExporterSupplierPostRendererVisitor(options))
}
