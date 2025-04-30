package com.quarkdown.core.pipeline.error

import com.quarkdown.core.function.Function
import com.quarkdown.core.log.Log

/**
 * Simple pipeline error handler that logs the error message.
 */
class BasePipelineErrorHandler : PipelineErrorHandler {
    override fun handle(
        error: PipelineException,
        sourceFunction: Function<*>?,
        action: () -> Unit,
    ) {
        val message = error.message ?: "Unknown error"
        Log.error(message)

        action()
    }
}
