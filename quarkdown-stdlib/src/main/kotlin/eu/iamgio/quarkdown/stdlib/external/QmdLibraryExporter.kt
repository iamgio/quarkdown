package eu.iamgio.quarkdown.stdlib.external

import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.library.LibraryExporter
import eu.iamgio.quarkdown.stdlib.includeResource
import java.io.Reader

/**
 * A [LibraryExporter] that loads a [Library] from a .qmd file.
 * This is destined to be used in other modules (such as `cli`) to load external libraries.
 * @param name library name
 * @param reader reader of the .qmd file
 */
class QmdLibraryExporter(private val name: String, private val reader: Reader) : LibraryExporter {
    override val library: Library
        get() =
            Library(
                name,
                functions = emptySet(),
                // The stdlib's includeResource function is used to include the content of the .qmd file
                onLoad = { context -> includeResource(context, reader) },
            )
}
