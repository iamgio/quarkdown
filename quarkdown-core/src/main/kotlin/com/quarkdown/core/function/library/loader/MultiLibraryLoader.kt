package com.quarkdown.core.function.library.loader

import com.quarkdown.core.function.library.Library

/**
 * A [LibraryLoader] that loads a library from a set of sources at once.
 * @param name name to assign to the library
 * @param loader strategy to load libraries from a single source with
 */
class MultiLibraryLoader<S>(
    private val name: String,
    private val loader: LibraryLoader<S>,
) : LibraryLoader<Set<S>> {
    override fun load(source: Set<S>): Library =
        Library(
            this.name,
            source
                .asSequence()
                .flatMap { loader.load(it).functions }
                .toSet(),
        )
}
