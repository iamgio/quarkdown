package com.quarkdown.core.context

import com.quarkdown.core.ast.attributes.MutableAstAttributes
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.function.Function
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.graph.Graph
import com.quarkdown.core.media.storage.MutableMediaStorage
import com.quarkdown.core.pipeline.Pipeline

/**
 * A context that is the result of a fork from an original parent [Context].
 * Several properties are inherited from it.
 * @param parent context this scope was forked from
 * @param subdocument the subdocument this context is processing
 */
class ScopeContext(
    val parent: Context,
    subdocument: Subdocument = parent.subdocument,
) : MutableContext(
        flavor = parent.flavor,
        libraries = emptySet(),
        subdocument = subdocument,
    ) {
    override val attachedPipeline: Pipeline?
        get() = super.attachedPipeline ?: parent.attachedPipeline

    override val documentInfo: DocumentInfo
        get() = parent.documentInfo

    override val options: MutableContextOptions
        get() = parent.options as? MutableContextOptions ?: MutableContextOptions()

    override val attributes: MutableAstAttributes
        get() = parent.attributes as? MutableAstAttributes ?: parent.attributes.toMutable()

    override val loadableLibraries: MutableSet<Library>
        get() = (parent as? MutableContext)?.loadableLibraries ?: super.loadableLibraries

    override val localizationTables
        get() = (parent as? MutableContext)?.localizationTables ?: parent.localizationTables.toMutableMap()

    override val mediaStorage: MutableMediaStorage
        get() = parent.mediaStorage as? MutableMediaStorage ?: MutableMediaStorage(options)

    override var subdocumentGraph: Graph<Subdocument>
        get() = parent.subdocumentGraph
        set(value) {
            (parent as? MutableContext)?.subdocumentGraph = value
        }

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
