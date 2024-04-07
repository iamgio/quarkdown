package eu.iamgio.quarkdown.function.library

/**
 * A compacter of a library project into a single [Library] object.
 * A library project must contain one class implementing this interface.
 */
interface LibraryExporter {
    /**
     * The library to export and hand to the pipeline.
     */
    val library: Library
}
