package com.quarkdown.stdlib.internal

import com.quarkdown.core.context.ChildContext
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.file.FileSystem

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
 * - When used in the root folder: `.pathtoroot` returns `.`
 * - When used in `<root>/subfolder`: `.pathtoroot` returns `..`
 * - When used in `<root>/subfolder1/subfolder2`: `.pathtoroot` returns `../..`
 *
 * @return a string value of the relative path to the root of the file system
 * @throws IllegalStateException if the relative path cannot be determined
 */
fun getRootFileSystem(
    context: Context,
    granularity: RootGranularity = RootGranularity.PROJECT,
): FileSystem? =
    when (granularity) {
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
