package com.quarkdown.core.function.error

import com.quarkdown.core.RUNTIME_ERROR_EXIT_CODE
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.function.Function

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
