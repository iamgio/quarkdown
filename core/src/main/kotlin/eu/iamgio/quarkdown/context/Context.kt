package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.LinkNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.ReferenceImage
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.UncheckedFunctionCall
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.pipeline.Pipeline

/**
 * Container of information about the current state of the pipeline, shared across the whole pipeline itself.
 */
interface Context {
    /**
     * The Markdown flavor in use.
     */
    val flavor: MarkdownFlavor

    /**
     * The pipeline this context is attached to, if it exists.
     * A context can have up to 1 attached pipeline.
     * @see eu.iamgio.quarkdown.pipeline.Pipelines.getAttachedPipeline
     * @see eu.iamgio.quarkdown.pipeline.Pipelines.attach
     */
    val attachedPipeline: Pipeline?

    /**
     * Mutable information about the final document that is being created.
     */
    val documentInfo: DocumentInfo

    /**
     * Whether there is at least one code block.
     * This is used to load the HighlightJS library in HTML rendering.
     */
    val hasCode: Boolean

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
     * Looks up a function by name.
     * @param name name of the function to look up, case-sensitive
     * @return the corresponding function, if it exists
     */
    fun getFunctionByName(name: String): Function<*>?

    /**
     * @param reference reference link to lookup
     * @return the corresponding link node, if it exists
     */
    fun resolve(reference: ReferenceLink): LinkNode?

    /**
     * @param call function call node to get a function call from
     * @return a new function call that [call] references to, with [call]'s arguments,
     * or `null` if [call] references to an unknown function
     */
    fun resolve(call: FunctionCallNode): FunctionCall<*>?

    /**
     * @param call function call node to get a function call from
     * @return an [UncheckedFunctionCall] that wraps the referenced function call if it has been resolved.
     * Calling `execute()` on an [UncheckedFunctionCall] whose function isn't resolved throws an exception
     * @see UncheckedFunctionCall
     * @see resolve
     */
    fun resolveUnchecked(call: FunctionCallNode): UncheckedFunctionCall<*>

    /**
     * @return a new scope context, forked from this context
     */
    fun fork(): ScopeContext
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
fun Context.resolveOrFallback(reference: ReferenceImage): Node =
    resolve(reference.link)?.let { Image(it, reference.width, reference.height) }
        ?: reference.link.fallback()
