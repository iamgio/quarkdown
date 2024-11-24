package eu.iamgio.quarkdown.test.util

import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.stdlib.external.QmdLibraryExporter
import java.io.File

/**
 * Library utilities for testing.
 */
object LibraryUtils {
    /**
     * Exports libraries from .qmd files.
     * @param names names of the libraries to export
     * @param directory directory containing the .qmd files
     * @return exported libraries, loaded from [directory] and matching [names] with a .qmd extension
     */
    fun export(
        names: Set<String>,
        directory: File,
    ): Set<Library> =
        names.map {
            QmdLibraryExporter(
                it,
                File(directory, "$it.qmd").reader(),
            ).library
        }.toSet()
}
