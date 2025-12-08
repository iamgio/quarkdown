package com.quarkdown.core.context

import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.function.Function
import com.quarkdown.core.pipeline.Pipeline

/**
 * A context that is the result of a fork from an original parent [Context].
 * All properties are inherited from it, but not all, such as libraries, are shared mutably.
 * @param parent context this scope was forked from
 * @param subdocument the subdocument this context is processing
 */
open class ScopeContext(
    override val parent: MutableContext,
    subdocument: Subdocument = parent.subdocument,
) : MutableContext(
        flavor = parent.flavor,
        libraries = emptySet(),
        subdocument = subdocument,
    ),
    ChildContext<MutableContext> {
    override val attachedPipeline: Pipeline?
        get() = super.attachedPipeline ?: parent.attachedPipeline

    override var documentInfo by parent::documentInfo
    override val options by parent::options
    override val attributes by parent::attributes
    override val loadableLibraries by parent::loadableLibraries
    override val localizationTables by parent::localizationTables
    override val mediaStorage by parent::mediaStorage
    override var subdocumentGraph by parent::subdocumentGraph

    /**
     * If no matching function is found among this [ScopeContext]'s own [libraries],
     * [parent]'s libraries are scanned.
     * @see Context.getFunctionByName
     */
    override fun getFunctionByName(name: String): Function<*>? = super.getFunctionByName(name) ?: parent.getFunctionByName(name)
}
