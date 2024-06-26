package eu.iamgio.quarkdown.pipeline

import eu.iamgio.quarkdown.pipeline.error.BasePipelineErrorHandler
import eu.iamgio.quarkdown.pipeline.error.PipelineErrorHandler

/**
 * Read-only settings that affect different behaviors of a [Pipeline].
 * @param prettyOutput whether the rendering stage should produce pretty output code
 * @param wrapOutput whether the rendered code should be wrapped in a template code.
 *                   For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`,
 *                   with the actual content injected in `body`
 * @param errorHandler the error handler strategy to use when an error occurs in the pipeline,
 *                     during the processing of a Quarkdown file
 */
data class PipelineOptions(
    val prettyOutput: Boolean = false,
    val wrapOutput: Boolean = true,
    val errorHandler: PipelineErrorHandler = BasePipelineErrorHandler(),
)
