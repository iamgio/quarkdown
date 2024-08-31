package eu.iamgio.quarkdown.media

import java.io.File

/**
 * A media that lives on the local filesystem.
 * @param file the local file where the media is stored
 */
data class LocalMedia(val file: File) : Media {
    override fun <T> accept(visitor: MediaVisitor<T>): T = visitor.visit(this)
}
