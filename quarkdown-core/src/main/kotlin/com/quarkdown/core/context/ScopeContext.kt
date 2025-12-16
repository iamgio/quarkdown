package com.quarkdown.core.context

import com.quarkdown.core.context.file.FileSystem

/**
 * A context that is the result of a fork from an original parent [Context].
 * All properties are inherited from it, but not all, such as libraries, are shared mutably.
 * @param parent context this scope was forked from
 * @param fileSystem file system to use in this context
 * @see SubdocumentContext to see what's inherited from the parent context
 */
open class ScopeContext(
    parent: MutableContext,
    override val fileSystem: FileSystem = parent.fileSystem,
) : SubdocumentContext(
        parent = parent,
        subdocument = parent.subdocument,
    ) {
    // A scope context shares the parent's document info.
    override var documentInfo by parent::documentInfo

    // Media registered in a scope is pushed to the parent's media storage.
    override val mediaStorage by parent::mediaStorage
}
