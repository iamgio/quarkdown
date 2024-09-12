package eu.iamgio.quarkdown.pipeline.error

import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.log.Log

/**
 * Pipeline error handler that rethrows the incoming error and ignores the additional custom action.
 * In a regular pipeline, this will cause the program to exit (see `QuarkdownCli` from the `cli` module).
 */
class StrictPipelineErrorHandler : PipelineErrorHandler {
    override fun handle(
        error: PipelineException,
        sourceFunction: Function<*>?,
        action: (String) -> Unit,
    ) {
        Log.error("An error occurred while in strict mode (error code ${error.code})")
        sourceFunction?.let { Log.error("Originated from function: ${it.name}") }
        throw error
    }
}
