package com.quarkdown.core.function.library.loader

import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.module.QuarkdownModule

/**
 * Creates a library from the set of functions exported in a [QuarkdownModule].
 * @param name name to assign to the library
 */
class MultiFunctionLibraryLoader(
    private val name: String,
) : LibraryLoader<QuarkdownModule> {
    override fun load(source: QuarkdownModule): Library = Library(this.name, source.toSet())

    /**
     * Creates a library from the functions exported in multiple [QuarkdownModule]s.
     */
    fun load(vararg sources: QuarkdownModule): Library = load(QuarkdownModule(*sources))
}
