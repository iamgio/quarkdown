package eu.iamgio.quarkdown.pipeline.error

import eu.iamgio.quarkdown.function.Function

/**
 * Strategy used to handle errors that may occur across the pipeline.
 */
interface PipelineErrorHandler {
    /**
     * Handles an exception thrown during any stage of the pipeline.
     * @param error exception to handle
     * @param sourceFunction function that threw the error, if it was thrown inside a function call
     * @param action additional custom error handler (with the error message as an argument)
     * @see BasePipelineErrorHandler
     * @see StrictPipelineErrorHandler
     */
    fun handle(
        error: PipelineException,
        sourceFunction: Function<*>?,
        action: (String) -> Unit,
    )
}
