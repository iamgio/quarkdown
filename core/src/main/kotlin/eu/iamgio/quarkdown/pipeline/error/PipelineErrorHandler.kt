package eu.iamgio.quarkdown.pipeline.error

import eu.iamgio.quarkdown.SystemProperties
import eu.iamgio.quarkdown.exitsOnError

/**
 * Strategy used to handle errors that may occur across the pipeline.
 */
interface PipelineErrorHandler {
    /**
     * Handles an exception thrown during any stage of the pipeline.
     * @param error exception to handle
     * @param action additional custom error handler (with the error message as an argument)
     * @see BasePipelineErrorHandler
     * @see StrictPipelineErrorHandler
     */
    fun handle(
        error: PipelineException,
        action: (String) -> Unit,
    )

    companion object {
        fun fromSystemProperties() =
            when {
                SystemProperties.exitsOnError -> StrictPipelineErrorHandler()
                else -> BasePipelineErrorHandler()
            }
    }
}
