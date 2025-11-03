package com.quarkdown.core.function.error

import com.quarkdown.core.function.call.FunctionCall

/**
 * An exception thrown when an error occurs inside a called function,
 * which may wrap another exception as its cause, such as an [IllegalStateException].
 * @param call the function call that caused the error
 * @param cause the error cause
 */
class FunctionCallRuntimeException(
    call: FunctionCall<*>,
    override val cause: Throwable,
) : InvalidFunctionCallException(
        call,
        reason = cause.message ?: "$cause (no further information)",
    )
