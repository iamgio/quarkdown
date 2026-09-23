package com.quarkdown.core.media.storage

import com.quarkdown.core.media.Media
import com.quarkdown.core.pipeline.output.OutputResource

/**
 * A storage of [Media] that can only be read.
 * The main purpose is exporting media to the output files.
 * For example, an image `![](img.png)` references a local image.
 * In this case, the HTML renderer would normally produce a `<img src="img.png">` tag, but the image would not be found in the output directory.
 * This means that the image must be copied to the output directory (not necessarily under the same name),
 * and the tag must reference the copied image path instead.
 * @see MutableMediaStorage
 */
interface ReadOnlyMediaStorage {
    /**
     * The name of the storage.
     * It also defines the name of the subdirectory in the output directory where media from this storage is saved.
     */
    val name: String

    /**
     * All the stored entries.
     */
    val all: Set<StoredMedia>

    /**
     * Whether this storage does not contain any media.
     */
    val isEmpty: Boolean
        get() = all.isEmpty()

    /**
     * Resolves a media by its path.
     * @param path path of the media. Can be a file path or a URL
     * @return the matching media, if any is found
     */
    fun resolve(path: String): StoredMedia?

    /**
     * Converts this storage to an [OutputResource].
     * This is used to export all media to the output directory.
     * Ideally, this method returns an [com.quarkdown.core.pipeline.output.OutputResourceGroup]
     * which contains all media inside of it.
     * @return an exportable resource containing all media
     */
    fun toResource(): OutputResource
}
