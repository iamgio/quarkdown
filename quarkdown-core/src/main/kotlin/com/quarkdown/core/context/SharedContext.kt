package com.quarkdown.core.context

import com.quarkdown.core.context.file.FileSystem
import com.quarkdown.core.function.Function

/**
 * A context that shares all of its properties with its parent [MutableContext].
 * This is useful when a context needs to be forked, for example to update its [fileSystem], but still its state mutably.
 * @param parent context this shared context was forked from
 * @param fileSystem file system to use in this context
 */
open class SharedContext(
    override val parent: MutableContext,
    override val fileSystem: FileSystem = parent.fileSystem,
) : MutableContext(
        flavor = parent.flavor,
        libraries = emptySet(),
        subdocument = parent.subdocument,
    ),
    ChildContext<MutableContext> {
    override val attachedPipeline by parent::attachedPipeline
    override var documentInfo by parent::documentInfo
    override val libraries by parent::libraries
    override val options by parent::options
    override val attributes by parent::attributes
    override val loadableLibraries by parent::loadableLibraries
    override val localizationTables by parent::localizationTables
    override val mediaStorage by parent::mediaStorage
    override var sharedSubdocumentsData by parent::sharedSubdocumentsData

    override fun getFunctionByName(name: String): Function<*>? = parent.getFunctionByName(name)
}
