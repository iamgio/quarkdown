package com.quarkdown.test.util

import com.quarkdown.core.function.library.Library
import com.quarkdown.stdlib.external.QmdLibraryExporter
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
