package com.quarkdown.core.function.library.loader

import com.quarkdown.core.function.library.Library

/**
 * A subsection of Quarkdown functions that can be exported via a [MultiFunctionLibraryLoader].
 */
typealias Module = Set<ExportableFunction>

/**
 * Creates a [Module] from a set of Kotlin functions.
 * @param functions the functions to export in the module
 */
fun moduleOf(vararg functions: ExportableFunction): Module = setOf(*functions)

/**
 * Creates a library from a set of Kotlin functions.
 * @param name name to assign to the library
 * @see FunctionLibraryLoader
 */
class MultiFunctionLibraryLoader(
    private val name: String,
) : LibraryLoader<Module> {
    override fun load(source: Module): Library = MultiLibraryLoader(this.name, FunctionLibraryLoader()).load(source)
}
