package com.quarkdown.core.function.library.loader

import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.module.QuarkdownModule

/**
 * Creates a library from a set of Kotlin functions exported in a [QuarkdownModule].
 * @param name name to assign to the library
 * @see FunctionLibraryLoader
 */
class MultiFunctionLibraryLoader(
    private val name: String,
) : LibraryLoader<QuarkdownModule> {
    override fun load(source: QuarkdownModule): Library =
        MultiLibraryLoader(this.name, FunctionLibraryLoader())
            .load(source)

    /**
     * Creates a library from a set of Kotlin functions exported in multiple [QuarkdownModule]s.
     */
    fun load(vararg sources: QuarkdownModule): Library = load(QuarkdownModule(*sources))
}
