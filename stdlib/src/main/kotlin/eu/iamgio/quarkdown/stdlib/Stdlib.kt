package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.library.LibraryExporter
import eu.iamgio.quarkdown.function.library.loader.MultiFunctionLibraryLoader

/**
 * Exporter of Quarkdown's standard library.
 */
object Stdlib : LibraryExporter {
    override val library: Library
        get() =
            MultiFunctionLibraryLoader(name = "stdlib").load(
                setOf(
                    ::test,
                    ::greet,
                    ::sum,
                ),
            )
}
