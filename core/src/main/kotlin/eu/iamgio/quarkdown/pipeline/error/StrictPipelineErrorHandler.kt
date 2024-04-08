package eu.iamgio.quarkdown.pipeline.error

import eu.iamgio.quarkdown.log.Log

/**
 * Pipeline error handler that rethrows the incoming error and ignores the additional custom action.
 */
class StrictPipelineErrorHandler : PipelineErrorHandler {
    override fun handle(
        error: PipelineException,
        action: (String) -> Unit,
    ) {
        Log.error("An error occurred while in strict mode (error code ${error.code})")
        throw error
    }
}
