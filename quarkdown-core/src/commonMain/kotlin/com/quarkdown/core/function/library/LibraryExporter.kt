package com.quarkdown.core.function.library

/**
 * A compacter of a library project into a single [Library] object.
 * A library project must contain one class implementing this interface.
 */
interface LibraryExporter {
    /**
     * The library to export and hand to the pipeline.
     */
    val library: Library

    companion object {
        /**
         * Loads libraries from the given exporters.
         * @param exporters library exporters
         * @return set of exported libraries from the given exporters
         */
        fun exportAll(vararg exporters: LibraryExporter): Set<Library> = exporters.map { it.library }.toSet()
    }
}
