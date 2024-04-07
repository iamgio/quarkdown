package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.MutableAstAttributes
import eu.iamgio.quarkdown.function.library.Library

/**
 * A mutable [Context] implementation, which allows registering new data to be looked up later.
 * @param attributes attributes of the node tree, which can be manipulated on demand
 */
class MutableContext(
    private val attributes: MutableAstAttributes = MutableAstAttributes(),
) : BaseContext(attributes) {
    override var hasMath: Boolean
        get() = attributes.hasMath
        set(value) {
            attributes.hasMath = value
        }

    override val libraries: Set<Library> = super.libraries.toMutableSet()

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
}
