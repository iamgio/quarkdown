package com.quarkdown.core.function.library.loader

import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.reflect.KFunctionAdapter
import com.quarkdown.core.function.value.OutputValue
import kotlin.reflect.KFunction

/**
 * Creates a library from a single Kotlin function.
 * @see KFunctionAdapter
 */
class FunctionLibraryLoader : LibraryLoader<KFunction<OutputValue<*>>> {
    override fun load(source: KFunction<OutputValue<*>>) = Library(source.name, setOf(KFunctionAdapter(source)))
}
