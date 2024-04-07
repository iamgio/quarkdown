package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.LinkNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.ReferenceImage
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.function.library.Library

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
     * Loaded libraries to look up functions from.
     */
    val libraries: Set<Library>

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
 * @param reference reference link to lookup
 * @return the corresponding looked up link node if it exists, its fallback node otherwise
 */
fun Context.resolveOrFallback(reference: ReferenceLink): Node = resolve(reference) ?: reference.fallback()

/**
 * @param reference reference image to lookup
 * @return the corresponding looked up image node if it exists, its fallback node otherwise
 */
fun Context.resolveOrFallback(reference: ReferenceImage): Node = resolve(reference.link)?.let { Image(it) } ?: reference.link.fallback()
