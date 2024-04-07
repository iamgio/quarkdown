package eu.iamgio.quarkdown.function.reflect.loaders

import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.library.LibraryLoader
import eu.iamgio.quarkdown.function.library.MultiLibraryLoader
import eu.iamgio.quarkdown.function.value.OutputValue
import kotlin.reflect.KFunction

/**
 * Creates a library from a set of Kotlin functions.
 * @param name name to assign to the library
 * @see FunctionLibraryLoader
 */
class MultiFunctionLibraryLoader(private val name: String) : LibraryLoader<Set<KFunction<OutputValue<*>>>> {
    override fun load(source: Set<KFunction<OutputValue<*>>>): Library {
        return MultiLibraryLoader(this.name, FunctionLibraryLoader()).load(source)
    }
}
