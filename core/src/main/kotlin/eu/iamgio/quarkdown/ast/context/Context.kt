package eu.iamgio.quarkdown.ast.context

import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.LinkNode
import eu.iamgio.quarkdown.ast.ReferenceLink

/**
 * Container of information about the current state of the pipeline, shared across the whole pipeline itself.
 */
interface Context {
    /**
     * @param reference reference link to lookup
     * @return the corresponding link node, if it exists
     */
    fun resolve(reference: ReferenceLink): LinkNode?
}

/**
 * An immutable [Context] implementation.
 * @param attributes attributes of the node tree, produced by the parsing stage
 */
open class BaseContext(private val attributes: AstAttributes) : Context {
    override fun resolve(reference: ReferenceLink): LinkNode? {
        return attributes.linkDefinitions.firstOrNull { it.label == reference.reference }
            ?.let { Link(reference.label, it.url, it.title) }
    }
}

/**
 * A mutable [Context] implementation, which allows registering new data to be looked up later.
 * @param attributes attributes of the node tree, which can be manipulated on demand
 */
class MutableContext(private val attributes: MutableAstAttributes = MutableAstAttributes()) : BaseContext(attributes) {
    /**
     * Registers a new [LinkDefinition], which can be later looked up
     * via [resolve] to produce a concrete link from a reference.
     * @param linkDefinition definition to register
     */
    fun register(linkDefinition: LinkDefinition) {
        attributes.linkDefinitions += linkDefinition
    }
}
