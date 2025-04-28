package eu.iamgio.quarkdown.function.library.loader

import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.reflect.KFunctionAdapter
import eu.iamgio.quarkdown.function.value.OutputValue
import kotlin.reflect.KFunction

/**
 * Creates a library from a single Kotlin function.
 * @see KFunctionAdapter
 */
class FunctionLibraryLoader : LibraryLoader<KFunction<OutputValue<*>>> {
    override fun load(source: KFunction<OutputValue<*>>) = Library(source.name, setOf(KFunctionAdapter(source)))
}
