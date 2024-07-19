package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.MutableAstAttributes
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.function.library.Library

/**
 * A mutable [Context] implementation, which allows registering new data to be looked up later.
 * @param flavor Markdown flavor used for this pipeline. It specifies how to produce the needed components
 * @param libraries loaded libraries to look up functions from
 * @param attributes attributes of the node tree, which can be manipulated on demand
 */
open class MutableContext(
    flavor: MarkdownFlavor,
    libraries: Set<Library> = emptySet(),
    private val attributes: MutableAstAttributes = MutableAstAttributes(),
) : BaseContext(attributes, flavor, libraries) {
    override var hasCode: Boolean by attributes::hasCode
    override var hasMath: Boolean by attributes::hasMath

    override val libraries: MutableSet<Library> = super.libraries.toMutableSet()

    override val options: MutableContextOptions = MutableContextOptions()

    /**
     * Registers a new [LinkDefinition], which can be later looked up
     * via [resolve] to produce a concrete link from a reference.
     * @param linkDefinition definition to register
     */
    open fun register(linkDefinition: LinkDefinition) {
        attributes.linkDefinitions += linkDefinition
    }

    /**
     * Enqueues a new [FunctionCallNode], which is executed in the next stage of the pipeline.
     * @param functionCall function call to register
     */
    open fun register(functionCall: FunctionCallNode) {
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
