package com.quarkdown.core.media

import com.quarkdown.core.filesystem.FsEntry

/**
 * A media that lives on a file system, physical or virtual.
 * @param file the entry where the media is stored
 */
data class LocalMedia(
    val file: FsEntry,
) : Media {
    override fun <T> accept(visitor: MediaVisitor<T>): T = visitor.visit(this)
}
