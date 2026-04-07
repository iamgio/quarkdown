package com.quarkdown.core.pipeline.output

import java.io.File

/**
 * An [OutputResource] backed by a file on the filesystem.
 * Instead of holding content in memory, it references a [file] that is copied to the output location on save.
 *
 * This is efficient for bundling large pre-existing files (e.g. third-party libraries)
 * where loading bytes into memory would be unnecessary overhead.
 *
 * @param name the output file name (with extension, since the original file name is used as-is)
 * @param file the source file to copy
 */
data class FileReferenceOutputResource(
    override val name: String,
    val file: File,
) : OutputResource {
    override fun <T> accept(visitor: OutputResourceVisitor<T>): T = visitor.visit(this)
}
