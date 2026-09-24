package com.quarkdown.core.filesystem

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
    val workingDirectory: FsEntry?

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
     * @return the resolved entry
     */
    fun resolve(path: String): FsEntry

    /**
     * Creates a new [FileSystem] branched from this one, with the given [workingDirectory].
     *
     * The [root] of the new file system is set to this file system if it has no root,
     * or to this file system's root otherwise.
     *
     * @param workingDirectory new working directory
     * @return the branched file system
     */
    fun branch(workingDirectory: FsEntry?): FileSystem

    /**
     * Creates a new root [FileSystem] on the same backend, with the given [workingDirectory]
     * and no branch lineage, so that [isRoot] holds for the result.
     * @param workingDirectory new working directory
     * @return the new root file system
     */
    fun reroot(workingDirectory: FsEntry?): FileSystem

    /**
     * Computes the relative path from this file system's [workingDirectory] to [other]'s.
     * @param other the target file system
     * @return the relative entry from this working directory to the other,
     *         or `null` if either working directory is `null` or no relative path exists
     */
    fun relativePathTo(other: FileSystem): FsEntry?
}
