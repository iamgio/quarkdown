package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.AstAttributes
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.LinkNode
import eu.iamgio.quarkdown.ast.base.inline.Image
import eu.iamgio.quarkdown.ast.base.inline.ReferenceImage
import eu.iamgio.quarkdown.ast.base.inline.ReferenceLink
import eu.iamgio.quarkdown.ast.quarkdown.FunctionCallNode
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.UncheckedFunctionCall
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.media.storage.ReadOnlyMediaStorage
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
     * Global properties that affect several behaviors
     * and that can be altered through function calls.
     */
    val options: ContextOptions

    /**
     * Information about the node tree that is being processed by the [attachedPipeline].
     */
    val attributes: AstAttributes

    /**
     * Loaded libraries to look up functions from.
     */
    val libraries: Set<Library>

    /**
     * Media storage that contains all the media files that are referenced within the document.
     * For example, if an image node references a local image file "image.png",
     * the local file needs to be exported to the output directory in order for a browser to look it up.
     * This storage is used to keep track of all the media files that may need to be exported.
     * @see eu.iamgio.quarkdown.context.hooks.MediaStorerHook
     */
    val mediaStorage: ReadOnlyMediaStorage

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
     * @return a new scope context, forked from this context, with the same base inherited properties
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
