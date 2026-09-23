package com.quarkdown.stdlib.external

import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.stdlib.includeResource

/**
 * A [LibraryExporter] that loads a [Library] from a .qd file.
 * This is destined to be used in other modules (such as `cli`) to load external libraries.
 * @param name library name
 * @param source lazy supplier of the .qd source code
 */
class QdLibraryExporter(
    private val name: String,
    private val source: () -> String,
) : LibraryExporter {
    override val library: Library by lazy {
        Library(
            name,
            functions = emptySet(),
            // The stdlib's includeResource function is used to include the content of the .qd file
            onLoad = { context -> includeResource(context, source()) },
        )
    }
}
