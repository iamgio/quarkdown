package com.quarkdown.core.function.library.loader

import com.quarkdown.core.function.Function
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.value.OutputValue

/**
 * A Quarkdown function that can be exported via a [FunctionLibraryLoader].
 */
typealias ExportableFunction = Function<out OutputValue<*>>

/**
 * Creates a library from a pre-built [Function].
 */
class FunctionLibraryLoader : LibraryLoader<ExportableFunction> {
    override fun load(source: ExportableFunction) = Library(source.name, setOf(source))
}
