package com.quarkdown.core.pipeline.error

import com.quarkdown.core.RUNTIME_ERROR_EXIT_CODE
import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.util.node.toPlainText

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
 * Converts this [Exception] to a [PipelineException] if it is not already one.
 */
fun Exception.asPipelineException(): PipelineException =
    this as? PipelineException ?: PipelineException(this.message ?: "An error occurred", RUNTIME_ERROR_EXIT_CODE)
