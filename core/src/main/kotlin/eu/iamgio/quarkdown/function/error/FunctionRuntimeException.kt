package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.RUNTIME_ERROR_EXIT_CODE
import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.pipeline.error.PipelineException

/**
 * An exception thrown when an error occurs inside a called function.
 * @param source function that threw the error
 * @param message error message
 */
class FunctionRuntimeException(
    val source: Function<*>,
    message: String,
) : PipelineException(message, RUNTIME_ERROR_EXIT_CODE) {
    /**
     * @param source function that threw the error
     * @param throwable the error cause
     */
    constructor(source: Function<*>, throwable: Throwable) : this(
        source,
        throwable.message ?: "$throwable (no further information)",
    )
}
