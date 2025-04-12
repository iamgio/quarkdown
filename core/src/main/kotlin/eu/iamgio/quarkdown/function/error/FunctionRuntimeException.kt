package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.RUNTIME_ERROR_EXIT_CODE
import eu.iamgio.quarkdown.ast.dsl.buildInline
import eu.iamgio.quarkdown.function.Function

/**
 * An exception thrown when an error occurs inside a called function.
 * @param source function that threw the error
 * @param cause the error cause
 */
class FunctionRuntimeException(
    source: Function<*>,
    override val cause: Throwable,
) : FunctionException(
        buildInline { text(cause.message ?: "$cause (no further information)") },
        RUNTIME_ERROR_EXIT_CODE,
        source,
    )
