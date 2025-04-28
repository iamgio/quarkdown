package eu.iamgio.quarkdown.pipeline.error

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.dsl.buildInline
import eu.iamgio.quarkdown.util.toPlainText

/**
 * An exception thrown during any stage of the pipeline.
 * @param richMessage formatted message to display. The actual [Exception] message is the plain text of it
 * @param code error code. If the program is running in strict mode and thus is killed,
 *             it defines the process exit code
 */
open class PipelineException(val richMessage: InlineContent, val code: Int) : Exception(richMessage.toPlainText()) {
    constructor(message: String, code: Int) : this(buildInline { text(message) }, code)
}
