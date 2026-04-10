package com.quarkdown.core.pipeline.output

import java.io.File

/**
 * An [OutputResource] backed by a file on the filesystem.
 * Instead of holding content in memory, it references a [file] that is efficiently copied to the output location.
 * @param name the output file name (with extension, since the original file name is used as-is)
 * @param file the source file (or directory) to copy
 */
data class FileReferenceOutputArtifact(
    override val name: String,
    val file: File,
) : OutputResource {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(this)
}
