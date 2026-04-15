package com.quarkdown.installlayout

import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import java.io.File

/**
 * A navigable entry (file or directory) within the Quarkdown install layout.
 */
interface InstallLayoutEntry {
    /** The filesystem location this entry points to. */
    val file: File

    /** Short name of this entry (file name) */
    val name: String
        get() = file.name

    /** Whether this entry exists on disk with the expected type (file vs. directory). */
    fun exists(): Boolean

    /** Resolves a child file relative to this entry's [file]. */
    fun resolveFile(relativePath: String): InstallLayoutFile = InstallLayoutFile(file.resolve(relativePath))

    /** Resolves a child directory relative to this entry's [file]. */
    fun resolveDirectory(relativePath: String): InstallLayoutDirectory = InstallLayoutDirectory(file.resolve(relativePath))

    /** Wraps this entry as an [OutputResource] for the pipeline to output. */
    fun asOutputResource(): OutputResource = FileReferenceOutputArtifact(name, file, useChecksumInvalidation = true)
}

/**
 * An [InstallLayoutEntry] that represents a regular file.
 * [exists] returns `true` only if the path is an existing regular file.
 */
data class InstallLayoutFile(
    override val file: File,
) : InstallLayoutEntry {
    override fun exists(): Boolean = file.isFile
}

/**
 * An [InstallLayoutEntry] that represents a directory.
 * [exists] returns `true` only if the path is an existing directory.
 */
data class InstallLayoutDirectory(
    override val file: File,
) : InstallLayoutEntry {
    override fun exists(): Boolean = file.isDirectory
}
