package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.MutableAstAttributes
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.pipeline.error.PipelineErrorHandler

/**
 * A mutable [Context] implementation, which allows registering new data to be looked up later.
 * @param flavor Markdown flavor used for this pipeline. It specifies how to produce the needed components
 * @param errorHandler the error handling strategy to use
 * @param attributes attributes of the node tree, which can be manipulated on demand
 */
class MutableContext(
    flavor: MarkdownFlavor,
    errorHandler: PipelineErrorHandler = PipelineErrorHandler.fromSystemProperties(),
    private val attributes: MutableAstAttributes = MutableAstAttributes(),
) : BaseContext(attributes, errorHandler = errorHandler, flavor = flavor) {
    override var hasCode: Boolean by attributes::hasCode
    override var hasMath: Boolean by attributes::hasMath

    override val libraries: MutableSet<Library> = super.libraries.toMutableSet()

    /**
     * Registers a new [LinkDefinition], which can be later looked up
     * via [resolve] to produce a concrete link from a reference.
     * @param linkDefinition definition to register
     */
    fun register(linkDefinition: LinkDefinition) {
        attributes.linkDefinitions += linkDefinition
    }

    /**
     * Enqueues a new [FunctionCallNode], which is executed in the next stage of the pipeline.
     * @param functionCall function call to register
     */
    fun register(functionCall: FunctionCallNode) {
        attributes.functionCalls += functionCall
    }

    /**
     * Returns a copy of the queue containing registered function calls and clears the original one.
     * @return all the registered function call nodes until now
     */
    fun dequeueAllFunctionCalls(): List<FunctionCallNode> =
        attributes.functionCalls.toList().also {
            attributes.functionCalls.clear()
        }
}
