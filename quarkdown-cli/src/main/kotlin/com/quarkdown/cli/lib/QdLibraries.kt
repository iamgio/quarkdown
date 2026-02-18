package com.quarkdown.cli.lib

import com.quarkdown.stdlib.external.QdLibraryExporter
import java.io.File

private const val EXTENSION_FILTER = "qd"

/**
 * Utilities for handling .qd libraries.
 */
object QdLibraries {
    /**
     * Loads all .qd libraries from a directory.
     * @param directory directory to load libraries from
     * @return set of [QdLibraryExporter]s
     */
    fun fromDirectory(directory: File): Set<QdLibraryExporter> {
        if (!directory.exists()) throw IllegalArgumentException("Libraries directory does not exist: $directory")
        if (!directory.isDirectory) throw IllegalArgumentException("Libraries directory is not a directory: $directory")

        return directory
            .listFiles()!!
            .asSequence()
            .filter { it.extension == EXTENSION_FILTER }
            .map { QdLibraryExporter(it.nameWithoutExtension) { it.reader() } }
            .toSet()
    }
}
