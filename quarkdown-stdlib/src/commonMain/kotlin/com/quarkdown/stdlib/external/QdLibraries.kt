package com.quarkdown.stdlib.external

import com.quarkdown.core.filesystem.FsEntry

private const val EXTENSION_FILTER = "qd"

/**
 * Utilities for handling .qd libraries.
 */
object QdLibraries {
    /**
     * Loads all .qd libraries from a directory, on any file system.
     * Sources are read lazily, when a library is first loaded.
     * @param directory directory to load libraries from
     * @return set of [QdLibraryExporter]s, one per `.qd` file directly inside [directory]
     * @throws IllegalArgumentException if [directory] does not exist or is not a directory
     */
    fun fromDirectory(directory: FsEntry): Set<QdLibraryExporter> {
        require(directory.exists) { "Libraries directory does not exist: ${directory.fullPath}" }
        require(directory.isDirectory) { "Libraries directory is not a directory: ${directory.fullPath}" }

        return directory
            .children()
            .asSequence()
            .filter { it.extension == EXTENSION_FILTER }
            .map { QdLibraryExporter(it.nameWithoutExtension) { it.readText() } }
            .toSet()
    }
}
