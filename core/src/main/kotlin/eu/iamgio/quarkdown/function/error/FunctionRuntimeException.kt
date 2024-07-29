package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.RUNTIME_ERROR_EXIT_CODE
import eu.iamgio.quarkdown.pipeline.error.PipelineException

/**
 * An exception thrown when an error occurs inside a called function.
 * @param message error message
 */
class FunctionRuntimeException(message: String) : PipelineException(message, RUNTIME_ERROR_EXIT_CODE) {
    /**
     * @param throwable the error cause
     */
    constructor(throwable: Throwable) : this(throwable.message ?: "$throwable (no further information)")
}
