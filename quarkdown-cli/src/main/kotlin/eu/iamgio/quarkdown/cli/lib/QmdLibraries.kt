package eu.iamgio.quarkdown.cli.lib

import eu.iamgio.quarkdown.stdlib.external.QmdLibraryExporter
import java.io.File

/**
 * Utilities for handling .qmd libraries.
 */
object QmdLibraries {
    /**
     * Loads all .qmd libraries from a directory.
     * @param directory directory to load libraries from
     * @return set of [QmdLibraryExporter]s
     */
    fun fromDirectory(directory: File): Set<QmdLibraryExporter> {
        if (!directory.exists()) throw IllegalArgumentException("Libraries directory does not exist: $directory")
        if (!directory.isDirectory) throw IllegalArgumentException("Libraries directory is not a directory: $directory")

        return directory
            .listFiles()!!
            .asSequence()
            .filter { it.extension == "qmd" }
            .map { QmdLibraryExporter(it.nameWithoutExtension, it.reader()) }
            .toSet()
    }
}
