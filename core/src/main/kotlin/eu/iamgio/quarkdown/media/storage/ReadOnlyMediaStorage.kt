package eu.iamgio.quarkdown.media.storage

import eu.iamgio.quarkdown.media.Media
import eu.iamgio.quarkdown.pipeline.output.OutputResource

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
     * All the stored entries.
     */
    val all: Set<StoredMedia>

    /**
     * Resolves a media by its path.
     * @param path path of the media. Can be a file path or a URL
     * @return the matching media, if any is found
     */
    fun resolve(path: String): StoredMedia?

    /**
     * Converts this storage to an [OutputResource].
     * This is used to export all media to the output directory.
     * Ideally, this method returns an [eu.iamgio.quarkdown.pipeline.output.OutputResourceGroup]
     * which contains all media inside of it.
     * @return an exportable resource containing all media
     */
    fun toResource(): OutputResource
}
