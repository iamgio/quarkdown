package com.quarkdown.core.util

import java.io.File
import kotlin.io.path.Path

/**
 * Utility methods for file-based operations.
 */
object IOUtils {
    /**
     * Resolves a [File] located in [path], either relative or absolute.
     * If the path is relative, the location is determined from the [workingDirectory].
     * @param path path of the file, either relative or absolute
     * @param workingDirectory directory from which the file is resolved, in case the path is relative
     * @return a [File] instance of the file
     */
    fun resolvePath(
        path: String,
        workingDirectory: File?,
    ): File =
        if (workingDirectory != null && !Path(path).isAbsolute) {
            File(workingDirectory, path)
        } else {
            File(path)
        }

    /**
     * Checks whether a [child] file is located in [parent] or any of its subdirectories.
     * @param parent the parent directory
     * @param child the child file
     * @return `true` if the child is located in the parent, `false` otherwise
     */
    fun isSubPath(
        parent: File,
        child: File,
    ): Boolean {
        val parentPath = parent.toPath().toAbsolutePath().normalize()
        val childPath = child.toPath().toAbsolutePath().normalize()
        return childPath.startsWith(parentPath)
    }
}
