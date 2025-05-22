package com.quarkdown.core.pipeline.error

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.context.Context
import com.quarkdown.core.function.error.FunctionException
import com.quarkdown.core.util.toPlainText

/**
 * An exception thrown during any stage of the pipeline.
 * @param richMessage formatted message to display. The actual [Exception] message is the plain text of it
 * @param code error code. If the program is running in strict mode and thus is killed,
 *             it defines the process exit code
 */
open class PipelineException(
    val richMessage: InlineContent,
    val code: Int,
) : Exception(richMessage.toPlainText()) {
    constructor(message: String, code: Int) : this(buildInline { text(message) }, code)
}

/**
 * Converts [this] exception to a renderable [Node], and performs the error handling provided by the [errorHandler] strategy.
 * @param errorHandler strategy to handle the error
 * @return [this] exception as a renderable [Node]
 */
fun PipelineException.asNode(errorHandler: PipelineErrorHandler): Node {
    // The function that the error originated from, if any.
    val sourceFunction = (this as? FunctionException)?.function

    return errorHandler.handle(this, sourceFunction) {
        Box.error(richMessage, title = sourceFunction?.name)
    }
}

/**
 * @param context context to use to retrieve the error handler from
 * @throws [this] exception if the context does not have an attached pipeline to retrieve the error handler from
 * @see asNode
 */
fun PipelineException.asNode(context: Context): Node =
    context.attachedPipeline
        ?.options
        ?.errorHandler
        ?.let(::asNode) ?: throw this
