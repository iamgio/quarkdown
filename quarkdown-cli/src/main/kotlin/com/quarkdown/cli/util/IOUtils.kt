package com.quarkdown.cli.util

import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * Cleans [this] directory by deleting all files and directories inside it.
 * Does nothing if the directory is empty or if the file does not exist or is not a directory.
 */
@OptIn(ExperimentalPathApi::class)
fun File.cleanDirectory() {
    listFiles()?.forEach { it.toPath().deleteRecursively() }
}
