package eu.iamgio.quarkdown.function.library

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext

/**
 * Component that is responsible for registering libraries in a pipeline's [Context],
 * in order to be looked up later.
 * @param context context to push libraries to
 */
class LibraryRegistrant(private val context: MutableContext) {
    /**
     * Registers a new single library, allowing it to be looked up.
     * @param library library to register
     */
    fun register(library: Library) {
        context.libraries += library
    }

    /**
     * Registers a new set of libraries, allowing them to be looked up.
     * @param libraries libraries to register
     */
    fun registerAll(libraries: Collection<Library>) {
        context.libraries.addAll(libraries)
    }
}
