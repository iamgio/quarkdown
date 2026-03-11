package com.quarkdown.core.function.call

import com.quarkdown.core.RUNTIME_ERROR_EXIT_CODE
import com.quarkdown.core.pipeline.error.PipelineException
import com.quarkdown.core.util.ScopedCounter

/**
 * Maximum allowed nesting depth for recursive function call execution.
 * Prevents stack overflows from infinite recursion in user-defined functions.
 */
private const val MAX_CALL_DEPTH = 512

/**
 * Tracks the current nesting depth of function call executions.
 */
internal val callDepth =
    ScopedCounter(MAX_CALL_DEPTH) {
        throw PipelineException(
            "Maximum function call depth ($MAX_CALL_DEPTH) exceeded. " +
                "This is likely caused by infinite recursion in a function call.",
            RUNTIME_ERROR_EXIT_CODE,
        )
    }
