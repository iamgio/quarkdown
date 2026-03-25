package com.quarkdown.core.context.file

import com.quarkdown.core.context.ChildContext
import com.quarkdown.core.context.Context

/**
 * Types of granularity for determining the root of the file system.
 */
enum class RootGranularity {
    /**
     * The root is the parent directory of the target file being processed by the `quarkdown compile` command
     */
    PROJECT,

    /**
     * The root is the parent directory of the current subdocument file.
     */
    SUBDOCUMENT,
}

/**
 * Retrieves the relative path to the root of the file system.
 * The root of the file system is determined by the working directory of the current subdocument.
 *
 * Example:
 *
 * - When used in the root folder: returns `.`
 * - When used in `<root>/subfolder`: returns `..`
 * - When used in `<root>/subfolder1/subfolder2`: returns `../..`
 *
 * @return a string value of the relative path to the root of the file system
 * @throws IllegalStateException if the relative path cannot be determined
 */
fun Context.getRootFileSystem(granularity: RootGranularity = RootGranularity.PROJECT): FileSystem? {
    val context = this
    return when (granularity) {
        RootGranularity.SUBDOCUMENT -> {
            context.fileSystem.root
        }

        RootGranularity.PROJECT -> {
            val rootContext = (context as? ChildContext<*>)?.root ?: context
            rootContext.attachedPipeline
                ?.options
                ?.workingDirectory
                ?.let(context.fileSystem::branch)
        }
    }
}
