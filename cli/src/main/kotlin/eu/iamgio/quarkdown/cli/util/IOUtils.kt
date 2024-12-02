package eu.iamgio.quarkdown.cli.util

import eu.iamgio.quarkdown.pipeline.output.FileResourceExporter
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import java.io.File

/**
 * The executable JAR file location, if available.
 */
val thisExecutableFile: File?
    get() = object {}.javaClass.protectionDomain?.codeSource?.location?.toURI()?.let(::File)

/**
 * Cleans [this] directory by deleting all files and directories inside it.
 * Does nothing if the directory is empty or if the file does not exist or is not a directory.
 */
fun File.cleanDirectory() {
    listFiles()?.forEach { it.deleteRecursively() }
}

/**
 * Saves [this] resource to file in a [directory].
 * @see FileResourceExporter
 * @return the saved file
 */
fun OutputResource.saveTo(directory: File): File {
    return accept(FileResourceExporter(location = directory))
}
