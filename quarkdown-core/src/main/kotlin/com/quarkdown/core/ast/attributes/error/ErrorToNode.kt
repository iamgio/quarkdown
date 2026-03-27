package com.quarkdown.core.ast.attributes.error

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.function.error.FunctionException
import com.quarkdown.core.function.error.InvalidFunctionCallException
import com.quarkdown.core.pipeline.error.PipelineErrorHandler
import com.quarkdown.core.pipeline.error.PipelineException

/**
 * Converts [this] exception to a renderable [Node], and performs the error handling provided by the [errorHandler] strategy.
 * @param errorHandler strategy to handle the error
 * @return [this] exception as a renderable [Node]
 */
fun Throwable.asNode(errorHandler: PipelineErrorHandler): Node {
    // The function that the error originated from, if any.
    val sourceFunction = (this as? FunctionException)?.function

    return errorHandler.handle(this, sourceFunction) {
        Box.error(
            message =
                when (this) {
                    is PipelineException -> this.richMessage
                    else -> buildInline { text(message ?: this::class.simpleName ?: "Unknown error") }
                },
            title = sourceFunction?.name,
            sourceText =
                (this as? InvalidFunctionCallException)
                    ?.call
                    ?.sourceNode
                    ?.sourceText
                    ?.trim(),
        )
    }
}
