package com.quarkdown.core.function.error

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.function.Function
import com.quarkdown.core.pipeline.error.PipelineException

/**
 * A [PipelineException] thrown when an error related to a function or function call occurs.
 * @param richMessage formatted message to display
 * @param function function the error is related to
 */
open class FunctionException(
    richMessage: InlineContent,
    code: Int,
    val function: Function<*>,
) : PipelineException(richMessage, code)
