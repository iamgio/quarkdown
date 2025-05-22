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

fun PipelineException.asNode(errorHandler: PipelineErrorHandler): Node {
    // The function that the error originated from.
    // Note that sourceFunction might be different from call.function if the error comes from a nested function call down the stack.
    val sourceFunction = (this as? FunctionException)?.function

    // The error is handled by the error handler strategy.
    return errorHandler.handle(this, sourceFunction) {
        // Shows an error message box in the final document.
        // If the exception is linked to a function, its name appears in the error title.
        Box.error(richMessage, title = sourceFunction?.name)
    }
}

fun PipelineException.asNode(context: Context): Node =
    context.attachedPipeline
        ?.options
        ?.errorHandler
        ?.let(::asNode) ?: throw this
