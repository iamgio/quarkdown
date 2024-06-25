package eu.iamgio.quarkdown.pipeline.options

import eu.iamgio.quarkdown.pipeline.error.PipelineErrorHandler
import java.io.File

/**
 * Read-only settings that affect different behaviors of a pipeline.
 */
interface PipelineOptions {
    /**
     * Whether the rendering stage should produce pretty output code.
     */
    val prettyOutput: Boolean

    /**
     * Whether the rendered code should be wrapped in a template code.
     * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`, with the actual content injected in `body`.
     * @see eu.iamgio.quarkdown.rendering.wrapper.RenderWrapper
     */
    val wrapOutput: Boolean

    /**
     * The error handling strategy to use.
     * @see eu.iamgio.quarkdown.pipeline.error
     */
    val errorHandler: PipelineErrorHandler

    /**
     * The output directory to save resource in, if set.
     */
    val outputDirectory: File?
}
