package com.quarkdown.core.media.storage

import com.quarkdown.core.media.Media
import com.quarkdown.core.pipeline.output.visitor.FileResourceExporter

/**
 * A media file paired with a name, used in a [ReadOnlyMediaStorage].
 * @param name symbolic name of the media. This is the name which elements in the document should refer to (e.g. a local image).
 *             This is reflected to the output file name in the output directory when the media is exported
 * @param media the media
 * @param storage reference to the storage that contains this media.
 */
data class StoredMedia(
    val name: String,
    val media: Media,
    val storage: ReadOnlyMediaStorage,
) {
    /**
     * Retrieves the path a stored media, starting from the output directory.
     * @param separator the separator to use in the path
     * @return the path to the media in the output directory, inside the [storage] subdirectory
     */
    fun path(separator: String): String = FileResourceExporter.NameProvider.stringToFileName(storage.name) + separator + name

    /**
     * Retrieves the path a stored media, starting from the output directory and using `/` as separator.
     * @see path
     */
    val path: String
        get() = path("/")
}
