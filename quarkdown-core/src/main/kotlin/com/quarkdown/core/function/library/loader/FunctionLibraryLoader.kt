package com.quarkdown.core.function.library.loader

import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.reflect.KFunctionAdapter
import com.quarkdown.core.function.value.OutputValue
import kotlin.reflect.KFunction

/**
 * A Quarkdown function that can be exported via a [FunctionLibraryLoader].
 */
typealias ExportableFunction = KFunction<OutputValue<*>>

/**
 * Creates a library from a single Kotlin function.
 * @see KFunctionAdapter
 */
class FunctionLibraryLoader : LibraryLoader<ExportableFunction> {
    override fun load(source: ExportableFunction) = Library(source.name, setOf(KFunctionAdapter(source)))
}
