package com.quarkdown.installlayout

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.toOutputResource

/**
 * A navigable entry (file or directory) within the Quarkdown install layout.
 */
interface InstallLayoutEntry {
    /** The file system location this entry points to. */
    val file: FsEntry

    /** Short name of this entry (file name) */
    val name: String
        get() = file.name

    /** Whether this entry exists with the expected type (file vs. directory). */
    fun exists(): Boolean

    /** Resolves a child file relative to this entry's [file]. */
    fun resolveFile(relativePath: String): InstallLayoutFile = InstallLayoutFile(file.resolve(relativePath))

    /** Resolves a child directory relative to this entry's [file]. */
    fun resolveDirectory(relativePath: String): InstallLayoutDirectory = InstallLayoutDirectory(file.resolve(relativePath))

    /**
     * Wraps this entry as an [OutputResource] for the pipeline to output.
     * Disk-backed entries are copied by reference, while virtual entries are materialized in memory.
     * @param symlink whether disk-backed entries should be symlinked instead of copied
     */
    fun asOutputResource(symlink: Boolean = false): OutputResource =
        file.toOutputResource(
            name,
            useChecksumInvalidation = true,
            symlink = symlink,
        )
}

/**
 * An [InstallLayoutEntry] that represents a regular file.
 * [exists] returns `true` only if the path is an existing regular file.
 */
data class InstallLayoutFile(
    override val file: FsEntry,
) : InstallLayoutEntry {
    override fun exists(): Boolean = file.isFile
}

/**
 * An [InstallLayoutEntry] that represents a directory.
 * [exists] returns `true` only if the path is an existing directory.
 */
data class InstallLayoutDirectory(
    override val file: FsEntry,
) : InstallLayoutEntry {
    override fun exists(): Boolean = file.isDirectory
}
