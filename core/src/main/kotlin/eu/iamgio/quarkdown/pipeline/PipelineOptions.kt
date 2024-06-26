package eu.iamgio.quarkdown.pipeline

import eu.iamgio.quarkdown.pipeline.error.BasePipelineErrorHandler
import eu.iamgio.quarkdown.pipeline.error.PipelineErrorHandler
import java.io.File

/**
 * Read-only settings that affect different behaviors of a [Pipeline].
 * @param prettyOutput whether the rendering stage should produce pretty output code
 * @param wrapOutput whether the rendered code should be wrapped in a template code.
 *                   For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`,
 *                   with the actual content injected in `body`
 * @param errorHandler the error handling strategy to use
 * @param outputDirectory the output directory to save resource in, if set
 */
data class PipelineOptions(
    val prettyOutput: Boolean = false,
    val wrapOutput: Boolean = true,
    val errorHandler: PipelineErrorHandler = BasePipelineErrorHandler(),
    val outputDirectory: File? = null,
)
