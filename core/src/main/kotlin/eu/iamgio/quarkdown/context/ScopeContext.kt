package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.MutableAstAttributes
import eu.iamgio.quarkdown.ast.quarkdown.FunctionCallNode
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.media.storage.MutableMediaStorage
import eu.iamgio.quarkdown.pipeline.Pipeline

/**
 * A context that is the result of a fork from an original parent [Context].
 * Several properties are inherited from it.
 * @param parent context this scope was forked from
 */
class ScopeContext(val parent: Context) : MutableContext(
    flavor = parent.flavor,
    libraries = emptySet(),
    mediaStorage = (parent.mediaStorage as? MutableMediaStorage) ?: MutableMediaStorage(),
) {
    override val attachedPipeline: Pipeline?
        get() = parent.attachedPipeline

    override val documentInfo: DocumentInfo
        get() = parent.documentInfo

    override val options: MutableContextOptions
        get() = parent.options as? MutableContextOptions ?: MutableContextOptions()

    override val attributes: MutableAstAttributes
        get() = parent.attributes as? MutableAstAttributes ?: parent.attributes.toMutable()

    /**
     * If no matching function is found among this [ScopeContext]'s own [libraries],
     * [parent]'s libraries are scanned.
     * @see Context.getFunctionByName
     */
    override fun getFunctionByName(name: String): Function<*>? = super.getFunctionByName(name) ?: parent.getFunctionByName(name)

    /**
     * Enqueues a function call to the [parent]'s queue if it is a [MutableContext],
     * or to this context otherwise.
     * This lets the registration go up the context tree so that it can be expanded
     * from the root context in the next stage of the pipeline.
     * @param functionCall function call to register
     * @see MutableContext.register
     */
    override fun register(functionCall: FunctionCallNode) {
        (parent as? MutableContext)?.register(functionCall)
            ?: super.register(functionCall)
    }

    /**
     * @param predicate condition to match
     * @return the last context (upwards, towards the root, starting from this context) that matches the [predicate],
     *         or `null` if no parent in the scope tree matches the given condition
     */
    fun lastParentOrNull(predicate: (Context) -> Boolean): Context? =
        when {
            // This is the last context to match the condition.
            predicate(this) && !predicate(parent) -> this
            // The root context matches the condition.
            parent !is ScopeContext && predicate(parent) -> parent
            // Scan the parent context.
            else -> (parent as? ScopeContext)?.lastParentOrNull(predicate)
        }
}
