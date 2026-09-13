package com.quarkdown.core.pipeline.output

import com.quarkdown.core.filesystem.FsEntry

/**
 * Converts this entry to an [OutputResource] for the pipeline to output.
 *
 * The entry is wrapped by reference as a [FileReferenceOutputArtifact], without loading its content:
 * at export time, disk-backed entries are efficiently copied (or symlinked),
 * while virtual entries are materialized by reading them through the file system.
 *
 * @param name the output resource name. Defaults to this entry's [FsEntry.name]
 * @param useChecksumInvalidation whether disk-backed entries should also carry a checksum file,
 *                                used by incremental builds to detect unchanged artifacts
 * @param symlink whether disk-backed entries should be symlinked instead of copied
 * @return the resource wrapping this entry
 */
fun FsEntry.toOutputResource(
    name: String = this.name,
    useChecksumInvalidation: Boolean = false,
    symlink: Boolean = false,
): OutputResource = FileReferenceOutputArtifact(name, this, useChecksumInvalidation, symlink)
