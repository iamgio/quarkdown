package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.AstAttributes
import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.LinkNode
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.UncheckedFunctionCall
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.pipeline.error.PipelineErrorHandler

/**
 * An immutable [Context] implementation.
 * @param attributes attributes of the node tree, produced by the parsing stage
 * @param flavor Markdown flavor used for this pipeline. It specifies how to produce the needed components
 * @param libraries loaded libraries to look up functions from
 * @param errorHandler the error handling strategy to use
 */
open class BaseContext(
    private val attributes: AstAttributes,
    override val flavor: MarkdownFlavor,
    override val libraries: Set<Library> = emptySet(),
    override val errorHandler: PipelineErrorHandler = PipelineErrorHandler.fromSystemProperties(),
) : Context {
    override val documentInfo = DocumentInfo()

    override val hasMath: Boolean
        get() = attributes.hasMath

    override val functionCalls: List<FunctionCallNode>
        get() = attributes.functionCalls

    override fun getFunctionByName(name: String): Function<*>? {
        return libraries.asSequence()
            .flatMap { it.functions }
            .find { it.name == name }
    }

    override fun resolve(reference: ReferenceLink): LinkNode? {
        return attributes.linkDefinitions.firstOrNull { it.label == reference.reference }
            ?.let { Link(reference.label, it.url, it.title) }
    }

    override fun resolve(call: FunctionCallNode): FunctionCall<*>? {
        val function = getFunctionByName(call.name)

        return function?.let {
            FunctionCall(it, call.arguments, context = this)
        }
    }

    override fun resolveUnchecked(call: FunctionCallNode): UncheckedFunctionCall<*> {
        return UncheckedFunctionCall(call.name) { resolve(call) }
    }
}
