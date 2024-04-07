package eu.iamgio.quarkdown.function.library

import eu.iamgio.quarkdown.function.Function

/**
 * A bundle of functions that can be called from a Quarkdown source.
 * @param name name of the library
 * @param functions functions the library makes available to call
 */
data class Library(
    val name: String,
    val functions: Set<Function<*>>,
)
