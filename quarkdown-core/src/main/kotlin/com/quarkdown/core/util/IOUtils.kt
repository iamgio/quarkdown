package com.quarkdown.core.util

import com.quarkdown.core.log.Log
import java.io.File
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createSymbolicLinkPointingTo
import kotlin.io.path.deleteRecursively

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
     * Computes an SHA-256 digest that represents the current state of [file].
     * - For a regular file, the digest covers its content.
     * - For a directory, the digest covers the sorted list of relative paths and file sizes,
     *   which is fast (metadata only) and catches additions, deletions, and size changes.
     */
    fun computeChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        if (file.isFile) {
            digest.update(file.readBytes())
        } else {
            file
                .walkTopDown()
                .filter { it.isFile }
                .sortedBy { it.relativeTo(file).path }
                .forEach {
                    digest.update(it.relativeTo(file).path.toByteArray())
                    digest.update(0)
                    digest.update(it.length().toString().toByteArray())
                    digest.update(0)
                }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Recursively deletes the file or directory at [path] without following symbolic links:
     * each link entry is removed, but the file or directory the link points to is left untouched.
     * No-op if [path] does not exist.
     */
    @OptIn(ExperimentalPathApi::class)
    fun deleteWithoutFollowingLinks(path: Path) = path.deleteRecursively()

    /**
     * Attempts to (re)create a symbolic link at [target] pointing to [source]. Returns `true` on
     * success, `false` if symbolic links are unavailable on this platform (caller should fall
     * back to a copy-based path).
     */
    fun trySymlink(
        target: Path,
        source: Path,
    ): Boolean =
        try {
            if (isAlreadySymlinkTo(target, source)) {
                Log.debug { "Symlink '${target.fileName}' already points to '$source'; reusing" }
            } else {
                createOrReplaceSymlinkAt(target, source)
                Log.debug { "Symlinked '${target.fileName}' to '$source'" }
            }
            true
        } catch (e: IOException) {
            Log.debug { "Symlink unavailable for '${target.fileName}' (${e.message}); falling back" }
            false
        }

    /** Whether [target] is already a symbolic link whose stored target is exactly [source]. */
    private fun isAlreadySymlinkTo(
        target: Path,
        source: Path,
    ): Boolean = Files.isSymbolicLink(target) && Files.readSymbolicLink(target) == source

    /**
     * Creates a symbolic link at [target] pointing to [source]. If a file, directory, or stale
     * symlink already occupies the target, it is cleared and the creation is retried.
     */
    private fun createOrReplaceSymlinkAt(
        target: Path,
        source: Path,
    ) {
        try {
            target.createSymbolicLinkPointingTo(source)
        } catch (_: FileAlreadyExistsException) {
            deleteWithoutFollowingLinks(target)
            target.createSymbolicLinkPointingTo(source)
        }
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
