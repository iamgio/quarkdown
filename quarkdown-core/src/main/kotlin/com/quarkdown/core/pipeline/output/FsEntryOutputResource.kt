package com.quarkdown.core.pipeline.output

import com.quarkdown.core.filesystem.FsEntry

/**
 * Converts this entry to an [OutputResource] for the pipeline to output.
 *
 * Disk-backed entries are wrapped as a [FileReferenceOutputArtifact], so they are efficiently
 * copied (or symlinked) by reference. Virtual entries are materialized into in-memory artifacts,
 * walking directories recursively.
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
): OutputResource =
    when (val file = toFileOrNull()) {
        null -> toInMemoryResource(name)
        else -> FileReferenceOutputArtifact(name, file, useChecksumInvalidation, symlink)
    }

/**
 * Materializes a virtual entry into an in-memory [OutputResource]:
 * a [BinaryOutputArtifact] for a file, or an [OutputResourceGroup]
 * of recursively materialized children for a directory.
 */
private fun FsEntry.toInMemoryResource(name: String): OutputResource =
    when {
        isDirectory -> OutputResourceGroup(name, children().map { it.toInMemoryResource(it.name) }.toSet())
        else -> BinaryOutputArtifact(name, readBytes().toList(), ArtifactType.AUTO)
    }
