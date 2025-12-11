package com.quarkdown.core.context.file

import com.quarkdown.core.util.IOUtils
import java.io.File
import java.nio.file.Path

/**
 * A file system abstraction which can retrieve files,
 * either absolutely or relative to a working directory.
 */
interface FileSystem {
    /**
     * The working directory of this file system.
     * If not `null`, [resolve] will be able to resolve relative paths
     * from this directory.
     */
    val workingDirectory: File?

    /**
     * The root file system that originated this one via [branch] calls.
     * If `null`, this file system is the root.
     */
    val root: FileSystem?

    /**
     * Whether this file system is the root one.
     */
    val isRoot: Boolean
        get() = root == null

    /**
     * Resolves a local file path, either absolutely or relatively from [workingDirectory].
     * This does not perform any check for file existence.
     * @param path absolute or relative file path to resolve
     * @return the resolved file
     */
    fun resolve(path: String): File

    /**
     * Creates a new [FileSystem] branched from this one, with the given [workingDirectory].
     *
     * The [root] of the new file system is set to this file system if it has no root,
     * or to this file system's root otherwise.
     *
     * @param workingDirectory new working directory
     * @return the branched file system
     */
    fun branch(workingDirectory: File?): FileSystem

    /**
     * Computes the relative path from this file system's [workingDirectory] to [other]'s.
     * @param other the target file system
     * @return the relative path from this working directory to the other,
     *         or `null` if either working directory is `null`
     */
    fun relativePathTo(other: FileSystem): Path?
}

/**
 * A simple [FileSystem] implementation that resolves paths
 * based on an optional working directory.
 */
internal data class SimpleFileSystem(
    override val workingDirectory: File? = null,
    override val root: FileSystem? = null,
) : FileSystem {
    override fun branch(workingDirectory: File?): FileSystem = SimpleFileSystem(workingDirectory, root ?: this)

    override fun resolve(path: String): File = IOUtils.resolvePath(path, workingDirectory)

    override fun relativePathTo(other: FileSystem): Path? {
        val from = this.workingDirectory?.toPath() ?: return null
        val to = other.workingDirectory?.toPath() ?: return null
        return try {
            from.relativize(to)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
