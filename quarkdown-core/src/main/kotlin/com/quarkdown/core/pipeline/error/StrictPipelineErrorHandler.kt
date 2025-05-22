package com.quarkdown.core.pipeline.error

import com.quarkdown.core.function.Function
import com.quarkdown.core.log.Log

/**
 * Pipeline error handler that rethrows the incoming error and ignores the additional custom action.
 * In a regular pipeline, this will cause the program to exit (see `QuarkdownCli` from the `cli` module).
 */
class StrictPipelineErrorHandler : PipelineErrorHandler {
    override fun <T> handle(
        error: PipelineException,
        sourceFunction: Function<*>?,
        action: () -> T,
    ): Nothing {
        Log.error("An error occurred while in strict mode (error code ${error.code})")
        sourceFunction?.let { Log.error("Originated from function: ${it.name}") }
        throw error
    }
}
