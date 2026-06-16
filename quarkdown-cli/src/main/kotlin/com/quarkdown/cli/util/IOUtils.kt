package com.quarkdown.cli.util

import com.quarkdown.core.util.IOUtils
import java.io.File
import kotlin.io.path.ExperimentalPathApi

/**
 * Cleans [this] directory by deleting all files and directories inside it.
 * Does nothing if the directory is empty or if the file does not exist or is not a directory.
 */
@OptIn(ExperimentalPathApi::class)
fun File.cleanDirectory() {
    listFiles()?.forEach { IOUtils.deleteWithoutFollowingLinks(it.toPath()) }
}
