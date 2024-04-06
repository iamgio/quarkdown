package eu.iamgio.quarkdown.ast.context

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.LinkNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.ReferenceImage
import eu.iamgio.quarkdown.ast.ReferenceLink

/**
 * Container of information about the current state of the pipeline, shared across the whole pipeline itself.
 */
interface Context {
    /**
     * Whether there is at least one math block or inline.
     * This is used to load the MathJax library in HTML rendering.
     */
    val hasMath: Boolean

    /**
     * The function calls to be expanded (executed) in the next stage of the pipeline.
     */
    val functionCalls: List<FunctionCallNode>

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
    override val hasMath: Boolean
        get() = attributes.hasMath

    override val functionCalls: List<FunctionCallNode>
        get() = attributes.functionCalls

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
    override var hasMath: Boolean
        get() = attributes.hasMath
        set(value) {
            attributes.hasMath = value
        }

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

/**
 * @param reference reference link to lookup
 * @return the corresponding looked up link node if it exists, its fallback node otherwise
 */
fun Context.resolveOrFallback(reference: ReferenceLink): Node = resolve(reference) ?: reference.fallback()

/**
 * @param reference reference image to lookup
 * @return the corresponding looked up image node if it exists, its fallback node otherwise
 */
fun Context.resolveOrFallback(reference: ReferenceImage): Node = resolve(reference.link)?.let { Image(it) } ?: reference.link.fallback()
