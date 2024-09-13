package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.pipeline.error.PipelineException

/**
 * A [PipelineException] thrown when an error related to a function or function call occurs.
 * @param function function the error is related to
 */
open class FunctionException(
    message: String,
    code: Int,
    val function: Function<*>,
) : PipelineException(message, code)
