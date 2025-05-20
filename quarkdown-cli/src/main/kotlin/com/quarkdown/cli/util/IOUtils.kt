package com.quarkdown.cli.util

import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * The executable JAR file location, if available.
 */
val thisExecutableFile: File?
    get() =
        object {}
            .javaClass.protectionDomain
            ?.codeSource
            ?.location
            ?.toURI()
            ?.let(::File)

/**
 * Cleans [this] directory by deleting all files and directories inside it.
 * Does nothing if the directory is empty or if the file does not exist or is not a directory.
 */
@OptIn(ExperimentalPathApi::class)
fun File.cleanDirectory() {
    listFiles()?.forEach { it.toPath().deleteRecursively() }
}
