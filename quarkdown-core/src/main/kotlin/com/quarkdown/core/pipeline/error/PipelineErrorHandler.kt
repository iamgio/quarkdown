package com.quarkdown.core.pipeline.error

import com.quarkdown.core.function.Function

/**
 * Strategy used to handle errors that may occur across the pipeline.
 */
interface PipelineErrorHandler {
    /**
     * Handles an exception thrown during any stage of the pipeline.
     * @param error exception to handle
     * @param sourceFunction function that threw the error, if it was thrown inside a function call
     * @param action additional custom error handler
     * @see BasePipelineErrorHandler
     * @see StrictPipelineErrorHandler
     * @return the result of the action
     */
    fun <T> handle(
        error: PipelineException,
        sourceFunction: Function<*>?,
        action: () -> T,
    ): T
}
