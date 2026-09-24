package com.quarkdown.core.pipeline.output

import com.quarkdown.core.filesystem.FsEntry

/**
 * An [OutputResource] backed by a filesystem entry.
 * Instead of holding content in memory, it references a [file] that the exporter reads from directly:
 * disk-backed entries are efficiently copied (or symlinked) by reference,
 * while virtual entries are materialized on the fly.
 * @param name the output file name (with extension, since the original file name is used as-is)
 * @param file the source entry (file or directory) to export
 * @param useChecksumInvalidation whether to also create a checksum file for this artifact, used for incremental builds
 *                                to determine whether the artifact has changed since the last build and should be recreated.
 *                                Only applies to disk-backed entries
 * @param symlink whether to create a symbolic link to the source file instead of copying it.
 *                Takes precedence over [useChecksumInvalidation]. Only applies to disk-backed entries
 */
data class FileReferenceOutputArtifact(
    override val name: String,
    val file: FsEntry,
    val useChecksumInvalidation: Boolean = false,
    val symlink: Boolean = false,
) : OutputResource {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(this)
}
