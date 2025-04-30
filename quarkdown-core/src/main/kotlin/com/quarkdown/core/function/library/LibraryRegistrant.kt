package com.quarkdown.core.function.library

import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext

/**
 * Component that is responsible for registering libraries in a pipeline's [Context],
 * in order to be looked up later.
 * @param context context to push libraries to
 */
class LibraryRegistrant(private val context: MutableContext) {
    /**
     * Registers a new single library, allowing it to be looked up by functions
     * and its [Library.onLoad] action is executed.
     * @param library library to register
     */
    fun register(library: Library) {
        context.libraries += library
        library.onLoad?.invoke(context)
    }

    /**
     * Registers a new set of libraries, allowing them to be looked up by functions.
     * and their [Library.onLoad] action is executed.
     * @param libraries libraries to register
     */
    fun registerAll(libraries: Collection<Library>) {
        libraries.forEach(::register)
    }
}
