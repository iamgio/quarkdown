package com.quarkdown.core.context

/**
 * A context that is the result of a fork from an original parent [Context].
 * All properties are inherited from it, but not all, such as libraries, are shared mutably.
 * @param parent context this scope was forked from
 * @param subdocument the subdocument this context is processing
 * @see SubdocumentContext to see what's inherited from the parent context
 */
open class ScopeContext(
    parent: MutableContext,
) : SubdocumentContext(
        parent = parent,
        subdocument = parent.subdocument,
    ) {
    // A scope context shares the parent's document info.
    override var documentInfo by parent::documentInfo
}
