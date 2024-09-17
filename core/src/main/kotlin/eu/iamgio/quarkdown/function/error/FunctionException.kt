package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.pipeline.error.PipelineException

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
