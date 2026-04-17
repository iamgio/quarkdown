package com.quarkdown.core.pipeline.output

import java.io.File

/**
 * An [OutputResource] backed by a file on the filesystem.
 * Instead of holding content in memory, it references a [file] that is efficiently copied to the output location.
 * @param name the output file name (with extension, since the original file name is used as-is)
 * @param file the source file (or directory) to copy
 * @param useChecksumInvalidation whether to also create a checksum file for this artifact, used for incremental builds
 *                                to determine whether the artifact has changed since the last build and should be recreated.
 */
data class FileReferenceOutputArtifact(
    override val name: String,
    val file: File,
    val useChecksumInvalidation: Boolean = false,
) : OutputResource {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(this)
}
