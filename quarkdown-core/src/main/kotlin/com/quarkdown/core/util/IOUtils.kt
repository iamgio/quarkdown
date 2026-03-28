package com.quarkdown.core.util

import java.io.File
import java.io.IOException
import java.nio.file.Path
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
     * Symlinks are resolved to their real paths when possible, so that a symlink inside [parent]
     * pointing outside it is correctly detected as not being a sub-path.
     * @param parent the parent directory
     * @param child the child file
     * @return `true` if the child is located in the parent, `false` otherwise
     */
    fun isSubPath(
        parent: File,
        child: File,
    ): Boolean {
        val parentPath = parent.toPath().resolveReal()
        val childPath = child.toPath().resolveReal()
        return childPath.startsWith(parentPath)
    }

    /**
     * Resolves a path to its real (symlink-resolved) form if the file exists,
     * falling back to absolute + normalized form for non-existent paths.
     */
    private fun Path.resolveReal(): Path =
        try {
            toRealPath()
        } catch (_: IOException) {
            toAbsolutePath().normalize()
        }
}
