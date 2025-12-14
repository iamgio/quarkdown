package com.quarkdown.core.context

import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.function.Function
import com.quarkdown.core.pipeline.Pipeline

/**
 * A context that is the result of a fork from an original parent [Context].
 * All properties are inherited from it, but not all, such as libraries, are shared mutably.
 *
 * This is mainly designed to be forked for subdocuments, where a new context is needed to process them.
 * Each subdocument context shares most properties with its parent context, but maintains its own state for certain aspects like document info.
 *
 * [ScopeContext] inherits from this class, and shares the parent's document info instead.
 *
 * @param parent context this scope was forked from
 * @param subdocument the subdocument this context is processing
 */
open class SubdocumentContext(
    override val parent: MutableContext,
    subdocument: Subdocument,
) : MutableContext(
        flavor = parent.flavor,
        libraries = emptySet(),
        subdocument = subdocument,
    ),
    ChildContext<MutableContext> {
    override val attachedPipeline: Pipeline?
        get() = super.attachedPipeline ?: parent.attachedPipeline

    // A subdocument inherits the parent's document info, but changes to it are local to this subdocument.
    override var documentInfo: DocumentInfo = parent.documentInfo

    override val options by parent::options
    override val attributes by parent::attributes
    override val loadableLibraries by parent::loadableLibraries
    override val localizationTables by parent::localizationTables
    override var subdocumentGraph by parent::subdocumentGraph

    /**
     * If no matching function is found among this [SubdocumentContext]'s own [libraries],
     * [parent]'s libraries are scanned.
     * @see Context.getFunctionByName
     */
    override fun getFunctionByName(name: String): Function<*>? = super.getFunctionByName(name) ?: parent.getFunctionByName(name)
}
